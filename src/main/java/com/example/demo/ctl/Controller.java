package com.example.demo.ctl;

import com.example.demo.api.game.GameOuterClass;
import jakarta.annotation.Nullable;

// everything becomes better with a control loop
interface Controller {
    @Nullable
    GameOuterClass.Game reconcile(GameOuterClass.Game game);
}
