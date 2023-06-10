package com.example.demo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MutexMap {
    private static class Shard {
        Lock intent = new ReentrantLock();
        ReadWriteLock control = new ReentrantReadWriteLock();
        Map<String, Lock> locks = new HashMap<>();
    }

    private final List<Shard> shards = new ArrayList<>(16);

    private Lock lockImpl(Shard shard, String key, int depth) {
        shard.control.readLock().lock();
        try {
            Lock currentLock = shard.locks.get(key);
            if (currentLock != null) {
                currentLock.lock();
                return currentLock;
            }
        } finally {
            shard.control.readLock().unlock();
        }

        shard.control.writeLock().lock();
        try {
            Lock lock = shard.locks.computeIfAbsent(key, (k) -> new ReentrantLock());
            if (depth == 5) {
                lock.lock();
                return lock;
            }
        } finally {
            shard.control.writeLock().unlock();
        }
        return lockImpl(shard, key, depth + 1);
    }

    private record Guard(Shard shard, String key, Lock lock) implements AutoCloseable {
        @Override
        public void close() throws Exception {
            lock.unlock();

        }
    }

    public AutoCloseable lock(String key) {
        //Shard shard = shards.get(key.hashCode() % shards.size());
        throw new UnsupportedOperationException();
    }
}
