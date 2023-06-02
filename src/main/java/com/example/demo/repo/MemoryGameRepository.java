package com.example.demo.repo;

import com.example.demo.api.game.GameOuterClass;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Repository
class MemoryGameRepository implements GameRepository {
    private static final RuntimeException CONFLICT = new RuntimeException();
    private final Map<String, GameOuterClass.Game> games = new ConcurrentHashMap<>();
    private final WatcherNotifier notifier;

    @Autowired
    MemoryGameRepository() {
        this.notifier = new WatcherNotifier();
    }

    @Override
    public void watch(GameWatcher watcher) {
        notifier.register(watcher);
    }

    @Nullable
    @Override
    public GameOuterClass.Game get(String id) {
        return games.get(id);
    }

    @Override
    public List<GameOuterClass.Game> list() {
        return games.values().stream().toList();
    }

    @Override
    public GameOuterClass.Game put(GameOuterClass.Game inputGame) throws ConflictException {
        GameOuterClass.Game game = inputGame
                .toBuilder()
                .setRevision(inputGame.getRevision() + 1)
                .build();
        String id = game.getId();
        if (inputGame.getRevision() == 0) {
            GameOuterClass.Game prev = games.putIfAbsent(id, game);
            if (prev != null) {
                throw new ConflictException();
            }
            Objects.requireNonNull(games.get(id));
        } else {
            Objects.requireNonNull(games.get(id));
            try {
                games.computeIfPresent(id, (k, existing) -> {
                    if (existing.getRevision() + 1 != game.getRevision()) {
                        throw CONFLICT;
                    }
                    return game;
                });
            } catch (RuntimeException ex) {
                if (ex == CONFLICT) {
                    throw new ConflictException();
                }
            }
        }
        notifier.deliver(game);
        return game;
    }
}
