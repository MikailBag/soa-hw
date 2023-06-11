package com.example.demo.repo;

import com.example.demo.api.chat.Chat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Repository
@ConditionalOnProperty("messaging.memory.enabled")
class MemoryMessageBroker implements MessageBroker, AutoCloseable {
    private final SubscriberNotifierMap notifier;

    @Autowired
    MemoryMessageBroker(
            @Value("${messaging.memory.buffer-size:256}") int bufferSize
    ) {
        this.notifier = new SubscriberNotifierMap(bufferSize);
    }

    @Override
    public <T> void publish(QueueDescriptor<T> descriptor, T item) throws IOException {
        try {
            notifier.deliver(descriptor, item);
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public <T> Subscription subscribe(QueueDescriptor<T> descriptor, Consumer<T> consumer) {
        return notifier.subscribe(descriptor, consumer);
    }

    @Override
    public void close() {
        notifier.close();
    }
}
