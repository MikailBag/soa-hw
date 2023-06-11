package com.example.demo.rpc;

import com.example.demo.repo.GameRepository;
import com.example.demo.service.GameActionsService;
import com.example.demo.service.UnknownGameException;
import io.grpc.Status;
import io.grpc.StatusException;

import java.time.Duration;

class Util {
    private Util() {
    }

    interface ApiHandler<T> {
        T handle() throws StatusException, GameRepository.ConflictException;
    }

    static <T> T retry(ApiHandler<T> handler) throws StatusException {
        for (int i = 0; i < 3; ++i) {
            try {
                return handler.handle();
            } catch (GameRepository.ConflictException ex) {
                try {
                    Thread.sleep(Duration.ofMillis(30));
                } catch (InterruptedException ignored) {
                    throw new StatusException(Status.CANCELLED);
                }
            }
        }
        throw new StatusException(Status.INTERNAL.withDescription("database is over-contended"));
    }

    static StatusException rethrow(UnknownGameException ignored) {
        return Status.NOT_FOUND.withDescription("Game not found").asException();
    }

    static StatusException rethrow(GameActionsService.ActionException ex) {
        Status status = switch (ex) {
            case GameActionsService.ActionAlreadyUsedException ignored -> Status.FAILED_PRECONDITION
                    .withDescription("You already used this action, wait for the next day");
            case GameActionsService.ActionNotMatchesRoleException ignored -> Status.FAILED_PRECONDITION
                    .withDescription("This action is not available for your role");
            case GameActionsService.ActionNotAvailableException ignored -> Status.FAILED_PRECONDITION
                    .withDescription("This action is not available in this game state");
        };
        return status.asException();
    }

    static StatusException rethrow(GameActionsService.InvalidTargetException ex) {
        return Status.INVALID_ARGUMENT
                .withDescription(ex.getMessage())
                .asException();
    }
}
