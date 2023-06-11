package com.example.demo.repo;

import com.example.demo.api.chat.Chat;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface MessageBroker {
    interface Subscription {
        void unsubscribe() throws InterruptedException;
    }

    <T> void publish(QueueDescriptor<T> descriptor, T item) throws IOException;

    <T> Subscription subscribe(QueueDescriptor<T> descriptor, Consumer<T> consumer);
}
