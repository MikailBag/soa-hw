package com.example.demo.ctl;

import com.example.demo.api.game.GameOuterClass;
import com.example.demo.repo.GameWatcher;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ControllerManager {
    private static final Logger log = LoggerFactory.getLogger(ControllerManager.class);
    private static final int MAX_ITER = 10;
    private final List<Controller> controllers;

    @Autowired
    ControllerManager(
            List<Controller> controllers
    ) {
        this.controllers = controllers;
    }

    @Nullable
    private GameOuterClass.Game iteration(GameOuterClass.Game game) {
        GameOuterClass.Game output = null;
        for (Controller c : controllers) {
            GameOuterClass.Game result = c.reconcile(game);
            if (result != null) {
                log.info("Controller {} modified game", c.getClass().getName());
                game = result;
                output = result;
            }
        }
        return output;
    }

    @Nullable
    public GameOuterClass.Game reconcile(GameOuterClass.Game game) {
        GameOuterClass.Game output = null;
        for (int i = 0; i < MAX_ITER; ++i) {
            GameOuterClass.Game result = iteration(game);
            if (result == null) {
                return output;
            }
            game = result;
            output = result;
        }
        throw new IllegalStateException("game failed to converge");
    }
}
