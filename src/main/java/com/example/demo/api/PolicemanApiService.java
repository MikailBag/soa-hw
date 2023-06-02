package com.example.demo.api;

import com.example.demo.api.policeman.Policeman;
import com.example.demo.api.policeman.PolicemanServiceGrpc;
import com.example.demo.service.GameActionsService;
import com.example.demo.service.UnknownGameException;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class PolicemanApiService extends PolicemanServiceGrpc.PolicemanServiceImplBase {
    private final GameActionsService gameActions;

    @Autowired
    PolicemanApiService(GameActionsService gameActions) {
        this.gameActions = gameActions;
    }

    @Override
    public void check(Policeman.CheckRequest request, StreamObserver<Policeman.CheckResponse> responseObserver) {
        try {
            Util.retry(() -> {
                try {
                    gameActions.processPolicemanCheck(
                            request.getRoomId(),
                            request.getParticipantId(),
                            request.getSuspectedId()
                    );
                } catch (UnknownGameException ex) {
                    throw Util.rethrow(ex);
                } catch (GameActionsService.InvalidTargetException ex) {
                    throw Util.rethrow(ex);
                } catch (GameActionsService.ActionException ex) {
                    throw Util.rethrow(ex);
                }
                return 0;
            });
            var response = Policeman.CheckResponse.newBuilder()
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusException ex) {
            responseObserver.onError(ex);
        }
    }

    @Override
    public void reveal(Policeman.RevealRequest request, StreamObserver<Policeman.RevealResponse> responseObserver) {
        super.reveal(request, responseObserver);
    }
}
