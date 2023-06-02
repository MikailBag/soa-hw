package com.example.demo.repo;

import com.example.demo.api.game.GameOuterClass;

public interface GameWatcher {
    void onApply(GameOuterClass.Game game);
}
