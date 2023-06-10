package com.example.demo.repo;

import com.example.demo.api.chat.Chat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.function.BiConsumer;

@Repository
@ConditionalOnProperty("messaging.memory.enabled")
class MemoryMessageBroker implements MessageBroker, AutoCloseable {
    private final SubscriberNotifier notifier;

    @Autowired
    MemoryMessageBroker(
            @Value("${messaging.memory.buffer-size:256}") int bufferSize
    ) {
        this.notifier = new SubscriberNotifier(bufferSize);
    }

    @Override
    public void publish(Chat.Topic topic, Chat.MessageData message) throws IOException {
        try {
            notifier.deliver(topic, message);
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Subscription subscribe(BiConsumer<Chat.Topic, Chat.MessageData> consumer) {
        return notifier.subscribe(consumer);
    }

    @Override
    public void close() {
        notifier.close();
    }
}
