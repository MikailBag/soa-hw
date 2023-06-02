package com.example.demo.repo;

import com.example.demo.api.game.GameOuterClass;
import jakarta.annotation.Nullable;

import java.util.List;

public interface GameRepository {
    @Nullable
    GameOuterClass.Game get(String id);

    List<GameOuterClass.Game> list();

    GameOuterClass.Game put(GameOuterClass.Game game) throws ConflictException;
    void watch(GameWatcher watcher);

    class ConflictException extends Exception {}
}
