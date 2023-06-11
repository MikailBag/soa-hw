package com.example.demo.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

class SubscriberNotifier implements AutoCloseable {
    private final Logger log = LoggerFactory.getLogger(MemoryMessageBroker.class);

    private final int bufferSize;
    private final Lock subscribersUpdates = new ReentrantLock();
    private boolean shuttingDown = false;
    private volatile List<CloseableQueue<Object>> subscribers = Collections.emptyList();
    private final ExecutorService executor;

    SubscriberNotifier(
            int bufferSize
    ) {
        this.bufferSize = bufferSize;
        this.executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual()
                .name("MemoryMessageBroker-SubscriptionInvoker", 0)
                .factory());
    }

    public void deliver(Object item) throws InterruptedException {
        List<CloseableQueue<Object>> snapshot = subscribers;
        log.info("Delivering message to {} subscribers", snapshot.size());
        for (CloseableQueue<Object> q : snapshot) {
            q.put(item);
        }
    }

    public MessageBroker.Subscription subscribe(Consumer<Object> consumer) {
        var queue = new CloseableQueue<Object>(bufferSize);
        var completed = new CountDownLatch(1);
        executor.execute(() -> {
            log.info("starting subscription");
            while (true) {
                try {
                    Object it = queue.take();
                    if (it == null) {
                        log.info("got null from take, stopping");
                        break;
                    }
                    consumer.accept(it);
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
