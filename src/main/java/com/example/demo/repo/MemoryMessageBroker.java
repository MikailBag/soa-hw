package com.example.demo.repo;

import com.example.demo.api.chat.Chat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

@Repository
@ConditionalOnProperty("messaging.memory.enabled")
class MemoryMessageBroker implements MessageBroker, AutoCloseable {
    private record Item(Chat.Topic topic, Chat.MessageData data) {
    }

    private final Logger log = LoggerFactory.getLogger(MemoryMessageBroker.class);

    private final int bufferSize;
    private final Lock subscribersUpdates = new ReentrantLock();
    private boolean shuttingDown = false;
    private volatile List<CloseableQueue<Item>> subscribers = Collections.emptyList();
    private final ExecutorService executor;

    @Autowired
    MemoryMessageBroker(
            @Value("${messaging.memory.buffer-size:256}") int bufferSize
    ) {
        this.bufferSize = bufferSize;
        this.executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual()
                .name("MemoryMessageBroker-SubscriptionInvoker", 0)
                .factory());
    }

    @Override
    public void publish(Chat.Topic topic, Chat.MessageData message) throws IOException {
        List<CloseableQueue<Item>> snapshot = subscribers;
        log.info("Publishing message to {} subscribers", snapshot.size());
        Item item = new Item(topic, message);
        for (CloseableQueue<Item> q : snapshot) {
            try {
                q.put(item);
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            }
        }
    }

    @Override
    public Subscription subscribe(BiConsumer<Chat.Topic, Chat.MessageData> consumer) {
        var queue = new CloseableQueue<Item>(bufferSize);
        var completed = new CountDownLatch(1);
        executor.execute(() -> {
            log.info("starting subscription");
            while (true) {
                try {
                    Item it = queue.take();
                    if (it == null) {
                        log.info("got null from take, stopping");
                        break;
                    }
                    consumer.accept(it.topic(), it.data());
                } catch (InterruptedException ex) {
                    log.error("worker thread was interrupted, some messages were lost");
                }
            }
            completed.countDown();
        });
        subscribersUpdates.lock();
        try {
            if (shuttingDown) {
                throw new IllegalStateException();
            }
            var newSubscribers = new ArrayList<>(subscribers);
            newSubscribers.add(queue);
            subscribers = newSubscribers;
        } finally {
            subscribersUpdates.unlock();
        }

        return () -> {
            log.info("unsubscribe: taking lock");
            subscribersUpdates.lock();
            try {
                log.info("unsubscribe: updating subscribers");
                var newSubscribers = new ArrayList<>(subscribers);
                newSubscribers.remove(queue);
                subscribers = newSubscribers;
            } finally {
                subscribersUpdates.unlock();
            }
            log.info("unsubscribe: closing queue");
            queue.close();
            log.info("unsubscribe: awaiting worker completion");
            completed.await();
            log.info("unsubscribe: done");
        };
    }

    @Override
    public void close() {
        log.info("acquiring lock");
        subscribersUpdates.lock();
        try {
            log.info("marking broker as closed");
            shuttingDown = true;
            log.info("ensuring no subscriptions are alive");
            if (!subscribers.isEmpty()) {
                throw new IllegalStateException();
            }
        } finally {
            subscribersUpdates.unlock();
        }
        log.info("shutting down the executor");
        executor.close();
    }
}
