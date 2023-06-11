package com.example.demo.repo;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

class SubscriberNotifierMap implements AutoCloseable {
    private final int bufferSize;
    private final Lock writeLock = new ReentrantLock();
    private final ConcurrentHashMap<String, SubscriberNotifier> notifiers = new ConcurrentHashMap<>();
    private boolean closed = false;

    public SubscriberNotifierMap(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    private SubscriberNotifier getNotifier(String name) {
        SubscriberNotifier notifier = notifiers.get(name);
        if (notifier != null) {
            return notifier;
        }
        writeLock.lock();
        try {
            if (closed) {
                throw new IllegalStateException("Can not use closed SubscriberNotifierMap");
            }
            return notifiers.computeIfAbsent(name, (k) -> new SubscriberNotifier(bufferSize));
        } finally {
            writeLock.unlock();
        }
    }

    public <T> void deliver(QueueDescriptor<T> descriptor, T item) throws InterruptedException {
        getNotifier(descriptor.name()).deliver(item);
    }

    public <T> MessageBroker.Subscription subscribe(QueueDescriptor<T> descriptor, Consumer<T> consumer) {
        return getNotifier(descriptor.name()).subscribe((obj) -> consumer.accept((T) obj));
    }

    @Override
    public void close() {
        var items = new ArrayList<SubscriberNotifier>();
        writeLock.lock();
        try {
            closed = true;
            items.addAll(notifiers.values());
            notifiers.clear();
        } finally {
            writeLock.unlock();
        }
        items.forEach(SubscriberNotifier::close);
    }
}
