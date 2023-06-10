package com.example.demo.repo;

import jakarta.annotation.Nullable;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

// MPSC
class CloseableQueue<T> {
    private static class MessageHolder<T> {
        @Nullable
        T inner = null;
    }
    private final ArrayBlockingQueue<MessageHolder<T>> inner;
    private volatile boolean closed = false;

    CloseableQueue(int capacity) {
        this.inner = new ArrayBlockingQueue<>(capacity);
    }

    void put(T item) throws InterruptedException {
        Objects.requireNonNull(item);
        var m = new MessageHolder<T>();
        m.inner = item;
        inner.put(m);
    }

    @Nullable
    T take() throws InterruptedException {
        if (closed) {
            if (inner.isEmpty()) {
                return null;
            }
        }
        MessageHolder<T> obj = inner.take();
        return obj.inner;
    }

    public void close() {
        closed = true;
        inner.offer(new MessageHolder<T>());
    }
}
