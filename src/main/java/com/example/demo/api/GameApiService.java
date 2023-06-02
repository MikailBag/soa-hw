package com.example.demo.api;

import com.example.demo.api.game.GameOuterClass;
import com.example.demo.api.game.GameServiceGrpc;
import com.example.demo.service.FilterService;
import com.example.demo.service.PollService;
import com.example.demo.service.RoomService;
import com.example.demo.service.UnknownGameException;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GameApiService extends GameServiceGrpc.GameServiceImplBase {
    private final RoomService roomService;
    private final PollService pollService;
    private final FilterService filterService;

    @Autowired
    GameApiService(
            RoomService roomService,
            PollService pollService,
            FilterService filterService
    ) {
        this.roomService = roomService;
        this.pollService = pollService;
        this.filterService = filterService;
    }

    @Override
    public void list(GameOuterClass.ListRequest request, StreamObserver<GameOuterClass.ListResponse> responseObserver) {
        var response = GameOuterClass.ListResponse.newBuilder();
        roomService.list().forEach((g) -> response.addGames(filterService.filter(g, "")));
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void create(
            GameOuterClass.CreateRequest request,
            StreamObserver<GameOuterClass.CreateResponse> responseObserver
    ) {
        try {
            GameOuterClass.Game created = roomService.create(request.getSettings(), request.getTitle());
            var response = GameOuterClass.CreateResponse.newBuilder();
            response.setGameId(created.getId());
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (RoomService.InvalidSettingsException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(ex.getDescription()).asException());
        }
    }

    @Override
    public void join(GameOuterClass.JoinRequest request, StreamObserver<GameOuterClass.JoinResponse> responseObserver) {
        GameOuterClass.Participant newParticipant;
        try {
            newParticipant = Util.retry(
                    () -> {
                        try {
                            return roomService.join(request.getRoomId(), request.getName());
                        } catch (RoomService.GameAlreadyStartedException ex) {
                            throw Status.FAILED_PRECONDITION
                                    .withDescription("Can not join already started game")
                                    .asException();
                        } catch (UnknownGameException ex) {
                            throw Util.rethrow(ex);
                        } catch (RoomService.NameAlreadyUsedException ex) {
                            throw Status.ALREADY_EXISTS
                                    .withDescription("Name is already used")
                                    .asException();
                        }
                    }
            );
        } catch (StatusException ex) {
            responseObserver.onError(ex);
            return;
        }
        var response = GameOuterClass.JoinResponse.newBuilder();
        response.setParticipantId(newParticipant.getId());
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void get(GameOuterClass.GetRequest request, StreamObserver<GameOuterClass.GetResponse> responseObserver) {
        GameOuterClass.Game game = roomService.get(request.getRoomId());
        if (game == null) {
            String msg = "game " + request.getRoomId() + " not found";
            responseObserver.onError(new StatusException(Status.NOT_FOUND.withDescription(msg)));
        } else {
            game = filterService.filter(game, request.getParticipantId());
            var res = GameOuterClass.GetResponse.newBuilder();
            res.setGame(game);
            responseObserver.onNext(res.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void poll(GameOuterClass.PollRequest request, StreamObserver<GameOuterClass.PollResponse> responseObserver) {
        var obs = (ServerCallStreamObserver<GameOuterClass.PollResponse>) responseObserver;
        Thread me = Thread.currentThread();
        if (!me.isVirtual()) {
            throw new IllegalStateException("not thread-per-request executor");
        }
        obs.setOnCancelHandler(me::interrupt);
        if (obs.isCancelled()) {
            return;
        }
        try {
            pollService.poll(request.getGameId(), request.getObservedRevision());
        } catch (UnknownGameException ignored) {
            String msg = "polled game " + request.getGameId() + " not found";
            responseObserver.onError(Status.NOT_FOUND.withDescription(msg).asException());
            return;
        } catch (InterruptedException ignored) {
            return;
        }
        GameOuterClass.Game game = roomService.get(request.getGameId());
        Objects.requireNonNull(game);
        var res = GameOuterClass.PollResponse.newBuilder();
        if (game.getRevision() > request.getObservedRevision()) {
            res.setRevision(game.getRevision());
        } else {
            res.setTimeout(true);
        }
        responseObserver.onNext(res.build());
        responseObserver.onCompleted();
    }
}
