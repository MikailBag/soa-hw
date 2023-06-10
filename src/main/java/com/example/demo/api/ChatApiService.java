package com.example.demo.api;

import com.example.demo.api.chat.Chat;
import com.example.demo.api.chat.ChatServiceGrpc;
import com.example.demo.service.ChatService;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
class ChatApiService extends ChatServiceGrpc.ChatServiceImplBase {
    private final ChatService svc;

    @Autowired
    ChatApiService(ChatService impl) {
        this.svc = impl;
    }

    @Override
    public void send(Chat.SendRequest request, StreamObserver<Chat.SendResponse> responseObserver) {
        if (request.getMessage().getParticipantId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("participant id unset").asException());
            return;
        }
        try {
            svc.send(request.getTopic(), request.getMessage());
        } catch (IOException ex) {
            Status s;
            if (((ServerCallStreamObserver<Chat.SendResponse>) responseObserver).isCancelled()) {
                s = Status.CANCELLED;
            } else {
                s = Status.INTERNAL;
            }
            responseObserver.onError(s.asException());
            return;
        } catch (ChatService.PermissionDeniedException ex) {
            responseObserver.onError(Status.PERMISSION_DENIED.withDescription(ex.getMessage()).asException());
            return;
        }
        var resp = Chat.SendResponse.newBuilder().build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void watch(Chat.WatchRequest request, StreamObserver<Chat.WatchEvent> responseObserver) {
        try {
            svc.subscribe(request.getTopic(), (msg) -> {
                Chat.WatchEvent ev = Chat.WatchEvent.newBuilder()
                        .setMessage(msg)
                        .build();
                responseObserver.onNext(ev);
                return !((ServerCallStreamObserver<Chat.WatchEvent>) responseObserver).isCancelled();
            }, request.getParticipantId());
            responseObserver.onCompleted();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch (ChatService.PermissionDeniedException ex) {
            responseObserver.onError(
                    Status.PERMISSION_DENIED
                            .withDescription(ex.getMessage())
                            .asException()
            );
        }
    }
}
