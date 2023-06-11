package com.example.demo.service;

import com.example.demo.api.game.GameOuterClass;
import com.example.demo.repo.GameRepository;
import com.example.demo.repo.GameWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PollService implements GameWatcher {
    private final Logger log = LoggerFactory.getLogger(PollService.class);

    private static class Waiter {
        int wakeAfter;
        CountDownLatch latch;
    }
    private static class WaiterSet {
        private List<Waiter> waiters = new ArrayList<>();
        private final Lock lock = new ReentrantLock();
        void add(Waiter waiter) {
            lock.lock();
            try {
                waiters.add(waiter);
            } finally {
                lock.unlock();
            }
        }

        void signal(int rev) {
            lock.lock();
            try {
                List<Waiter> newWaiters = new ArrayList<>();
                for (Waiter w : waiters) {
                    if (w.wakeAfter >= rev) {
                        newWaiters.add(w);
                    } else {
                        w.latch.countDown();
                    }
                }
                waiters = newWaiters;
            } finally {
                lock.unlock();
            }
        }
    }
    private final GameRepository repository;
    private final ConcurrentHashMap<String, WaiterSet> waitersMap = new ConcurrentHashMap<>();


    @Autowired
    PollService(GameRepository repository) {
        this.repository = repository;
        repository.watch(this);
    }

    public void poll(String id, int wakeAfter) throws UnknownGameException, InterruptedException {
        log.debug("poll started, wakeAfter={}", wakeAfter);
        var latch = startWatch(id, wakeAfter);
        GameOuterClass.Game game = repository.get(id);
        if (game == null) {
            throw new UnknownGameException();
        }
        if (game.getRevision() > wakeAfter) {
            log.debug("Aborting poll: game is already newer");
            return;
        }
        var ok = latch.await(10, TimeUnit.SECONDS);
        if (ok) {
            log.debug("Finishing poll: game changed");
        } else {
            log.debug("Poll timed out");
        }
    }

    private WaiterSet waitersFor(String id) {
        return waitersMap.computeIfAbsent(id, (k) -> new WaiterSet());
    }

    private CountDownLatch startWatch(String id, int wakeAfter) {
        var waiter = new Waiter();
        waiter.latch = new CountDownLatch(1);
        waiter.wakeAfter = wakeAfter;
        waitersFor(id).add(waiter);
        return waiter.latch;
    }


    @Override
    public void onApply(GameOuterClass.Game game) {
        log.debug("Notifying observers for change: game {}, revision {}", game.getId(), game.getRevision());
        waitersFor(game.getId()).signal(game.getRevision());
    }
}
