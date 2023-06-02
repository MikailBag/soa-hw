package com.example.demo.api;

import com.example.demo.api.citizen.Citizen;
import com.example.demo.api.citizen.CitizenServiceGrpc;
import com.example.demo.service.GameActionsService;
import com.example.demo.service.UnknownGameException;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CitizenApiService extends CitizenServiceGrpc.CitizenServiceImplBase {
    private final GameActionsService gameActions;

    @Autowired
    CitizenApiService(GameActionsService gameActions) {
        this.gameActions = gameActions;
    }

    @Override
    public void vote(Citizen.CitizenVoteRequest request, StreamObserver<Citizen.CitizenVoteResponse> responseObserver) {
        try {
            var response = Util.retry(() -> {
                try {
                    gameActions.processCitizenVote(
                            request.getRoomId(),
                            request.getParticipantId(),
                            request.getSuspectedId()
                    );
                } catch (UnknownGameException ex) {
                    throw Util.rethrow(ex);
                } catch (GameActionsService.ActionException ex) {
                    throw Util.rethrow(ex);
                } catch (GameActionsService.InvalidTargetException ex) {
                    throw Util.rethrow(ex);
                }

                return Citizen.CitizenVoteResponse.newBuilder().build();
            });
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusException ex) {
            responseObserver.onError(ex);
        }
    }
}
