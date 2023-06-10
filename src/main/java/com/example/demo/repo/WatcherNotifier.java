package com.example.demo.repo;

import com.example.demo.api.game.GameOuterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class WatcherNotifier {
    private static final Logger log = LoggerFactory.getLogger(WatcherNotifier.class);

    private static class WatchState {
        Lock lock = new ReentrantLock();
        int lastDeliveredRevision = 0;
    }

    private final ReadWriteLock watchersLock = new ReentrantReadWriteLock();
    private final List<GameWatcher> watchers = new ArrayList<>();
    private final ConcurrentHashMap<String, WatchState> games = new ConcurrentHashMap<>();
    private final Executor exec;

    WatcherNotifier() {
        this.exec = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual()
                        .name("GameWatcherNotifier-", 0)
                        .factory()
        );
    }

    void register(GameWatcher watcher) {
        watchersLock.writeLock().lock();
        try {
            watchers.add(watcher);
        } finally {
            watchersLock.writeLock().unlock();
        }
    }

    void deliver(GameOuterClass.Game game) {
        WatchState state = games.computeIfAbsent(game.getId(), (k) -> new WatchState());
        watchersLock.readLock().lock();
        state.lock.lock();
        try {
            if (game.getRevision() > state.lastDeliveredRevision) {
                state.lastDeliveredRevision = game.getRevision();
                for (GameWatcher w : watchers) {
                    exec.execute(() -> {
                        try {
                            w.onApply(game);
                        } catch (Throwable ex) {
                            log.error("watcher throwed exception", ex);
                            throw ex;
                        }
                    });
                }
            }
        } finally {
            state.lock.unlock();
            watchersLock.readLock().unlock();
        }
    }
}
