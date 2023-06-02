package com.example.demo.ctl;

import com.example.demo.api.game.GameOuterClass;
import com.example.demo.repo.GameRepository;
import com.example.demo.repo.GameWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ControllerRunner implements GameWatcher {
    private static final Logger log = LoggerFactory.getLogger(ControllerRunner.class);
    private static final String MDC_KEY = "reconcileTarget";
    private final ControllerManager controllerManager;
    private final GameRepository repository;

    @Autowired
    ControllerRunner(
            ControllerManager controllerManager,
            GameRepository repository
    ) {
        this.controllerManager = controllerManager;
        this.repository = repository;
        repository.watch(this);
    }

    private void onApplyImpl(GameOuterClass.Game game) {
        log.info("Starting reconciliation");
        game = controllerManager.reconcile(game);
        if (game == null) {
            log.info("No changes");
            return;
        }
        try {
            repository.put(game);
            log.info("Successfully applied new game");
        } catch (GameRepository.ConflictException ignored) {
            log.info("Got a conflict");
        }
    }


    @Override
    public void onApply(GameOuterClass.Game game) {
        MDC.put(MDC_KEY, game.getId() + "@" + game.getRevision());
        try {
            onApplyImpl(game);
        } finally {
            MDC.popByKey(MDC_KEY);
        }
    }
}
