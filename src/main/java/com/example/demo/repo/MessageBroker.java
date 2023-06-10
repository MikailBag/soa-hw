package com.example.demo.repo;

import com.example.demo.api.chat.Chat;

import java.io.IOException;
import java.util.function.BiConsumer;

public interface MessageBroker {
    interface Subscription {
        void unsubscribe() throws InterruptedException;
    }
    void publish(Chat.Topic topic, Chat.MessageData message) throws IOException;
    Subscription subscribe(BiConsumer<Chat.Topic, Chat.MessageData> consumer);
}
