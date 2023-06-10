package com.example.demo.service;

import com.example.demo.api.game.GameOuterClass;
import com.example.demo.repo.GameRepository;
import com.example.demo.repo.GameWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Lazy(false)
public class NotificationSender implements GameWatcher {
    private final Logger log = LoggerFactory.getLogger(NotificationSender.class);

    private static class State {
        int revision = 0;
        Set<String> participants = new HashSet<>();
        boolean gameStarted = false;
    }

    private final Lock applyLock = new ReentrantLock();
    private final ConcurrentHashMap<String, State> states = new ConcurrentHashMap<>();
    private final ChatService chatService;

    @Autowired
    NotificationSender(
            GameRepository repository,
            ChatService chatService
    ) {
        this.chatService = chatService;
        repository.watch(this);
    }

    private void processUpdatedGame(GameOuterClass.Game game) {
        State s = states.computeIfAbsent(game.getId(), (k) -> new State());
        if (s.revision >= game.getRevision()) {
            log.info("ignoring out-of-order update");
            return;
        }
        s.revision = game.getRevision();
        List<GameOuterClass.Participant> newParticipants = game
                .getParticipantsList()
                .stream()
                .filter(p -> !s.participants.contains(p.getId()))
                .toList();
        newParticipants.forEach(p -> s.participants.add(p.getId()));
        newParticipants.forEach(p -> chatService.sendSystemMessage(game.getId(), p.getName() + " joined!"));
        if (!s.gameStarted && game.getState().equals(GameOuterClass.Game.State.IN_PROGRESS)) {
            s.gameStarted = true;
            chatService.sendSystemMessage(game.getId(), "game started");
        }
        if (game.getState().equals(GameOuterClass.Game.State.FINISHED)) {
            chatService.sendSystemMessage(game.getId(), "game finished");
            states.remove(game.getId());
        }
    }

    @Override
    public void onApply(GameOuterClass.Game game) {
        MDC.put("gameId", game.getId());
        log.info("processing updates");

        applyLock.lock();
        try {
            processUpdatedGame(game);
        } finally {
            applyLock.unlock();
            MDC.popByKey("gameId");
        }
    }
}

