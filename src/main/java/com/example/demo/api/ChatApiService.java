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

import java.util.UUID;

@Component
class ChatApiService extends ChatServiceGrpc.ChatServiceImplBase {
    private final ChatService impl;

    @Autowired
    ChatApiService(ChatService impl) {
        this.impl = impl;
    }

    @Override
    public void send(Chat.SendRequest request, StreamObserver<Chat.SendResponse> responseObserver) {
        if (!request.getMessage().getParticipantId().isEmpty()) {
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT));
        }
        impl.send(
                request.getRoomId(),
                new ChatService.Message(request.getMessage().getBody(), request.getParticipantId())
        );
    }

    @Override
    public void watch(Chat.WatchRequest request, StreamObserver<Chat.WatchEvent> responseObserver) {
        String id = UUID.randomUUID().toString();
        try {
            impl.subscribe(request.getRoomId(), id, (m) -> {
                Chat.Message msg = Chat.Message.newBuilder()
                        .setBody(m.body())
                        .setParticipantId(m.author())
                        .build();
                Chat.WatchEvent ev = Chat.WatchEvent.newBuilder()
                        .setMessage(msg)
                        .build();
                responseObserver.onNext(ev);
                return !((ServerCallStreamObserver<Chat.WatchEvent>) responseObserver).isCancelled();
            });
            responseObserver.onCompleted();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch (ChatService.AlreadySubscribedException ex) {
            // currently unreachable
            throw new RuntimeException(ex);
        }
    }
}
