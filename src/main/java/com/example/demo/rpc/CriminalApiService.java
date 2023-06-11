package com.example.demo.rpc;

import com.example.demo.api.criminal.Criminal;
import com.example.demo.api.criminal.CriminalServiceGrpc;
import com.example.demo.service.GameActionsService;
import com.example.demo.service.UnknownGameException;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class CriminalApiService extends CriminalServiceGrpc.CriminalServiceImplBase {
    private final GameActionsService gameActions;

    @Autowired
    CriminalApiService(GameActionsService gameActions) {
        this.gameActions = gameActions;
    }

    @Override
    public void vote(
            Criminal.CriminalVoteRequest request,
            StreamObserver<Criminal.CriminalVoteResponse> responseObserver
    ) {
        try {
            Util.retry(
                    () -> {
                        try {
                            gameActions.processCriminalVote(
                                    request.getRoomId(),
                                    AuthnInterceptor.CTX_KEY.get(),
                                    request.getVictimId()
                            );
                        } catch (GameActionsService.ActionException ex) {
                            throw Util.rethrow(ex);
                        } catch (GameActionsService.InvalidTargetException ex) {
                            throw Util.rethrow(ex);
                        } catch (UnknownGameException ex) {
                            throw Util.rethrow(ex);
                        }
                        return 0;
                    }
            );
            var response = Criminal.CriminalVoteResponse
                    .newBuilder()
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusException ex) {
            responseObserver.onError(ex);
        }
    }
}
