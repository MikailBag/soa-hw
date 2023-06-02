package com.example.demo.ctl;

import com.example.demo.api.game.GameOuterClass;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class NightEndController implements Controller {
    private static final Logger log = LoggerFactory.getLogger(NightEndController.class);

    @Nullable
    @Override
    public GameOuterClass.Game reconcile(GameOuterClass.Game game) {
        if (!game.getState().equals(GameOuterClass.Game.State.IN_PROGRESS)) {
            return null;
        }
        if (!game.hasNight()) {
            return null;
        }
        GameOuterClass.NightState night = game.getNight();
        if (!night.getCriminalVoteComplete()) {
            log.info("waiting for criminal vote to end");
            return null;
        }
        Set<GameOuterClass.Role> innocentRoles = Set.of(GameOuterClass.Role.NORMAL, GameOuterClass.Role.POLICEMAN);

        String victim = game.getNight().getVictimId();
        boolean innocentRemain = game
                .getParticipantsList()
                .stream()
                .filter(p -> p.getState().equals(GameOuterClass.Participant.State.ALIVE))
                .filter(p -> !p.getId().equals(victim))
                .anyMatch(p -> innocentRoles.contains(p.getRole()));
        if (!innocentRemain) {
            log.info("finishing game, criminals won");
            return game
                    .toBuilder()
                    .setState(GameOuterClass.Game.State.FINISHED)
                    .setOutcome(GameOuterClass.Game.Outcome.CRIMINALS_WON)
                    .build();
        }
        Set<String> alivePolicemen = game
                .getParticipantsList()
                .stream()
                .filter(p -> p.getRole().equals(GameOuterClass.Role.POLICEMAN))
                .filter(p -> p.getState().equals(GameOuterClass.Participant.State.ALIVE))
                .map(GameOuterClass.Participant::getId)
                .collect(Collectors.toSet());
        Set<String> votedPolicemen = game
                .getNight()
                .getPolicemanChecksList()
                .stream()
                .map(GameOuterClass.PolicemanCheck::getPolicemanId)
                .collect(Collectors.toSet());
        if (!votedPolicemen.containsAll(alivePolicemen)) {
            log.info("waiting for policemen to run their checks");
            return null;
        }
        log.info("finishing night");
        if (game.getHistoryList().isEmpty()) {
            throw new IllegalStateException("history is empty");
        }
        if (!victim.isEmpty()) {
            List<GameOuterClass.Participant> newParticipants = game
                    .getParticipantsList()
                    .stream()
                    .map(p -> {
                        if (p.getId().equals(victim)) {
                            return p.toBuilder()
                                    .setState(GameOuterClass.Participant.State.KILLED_AT_NIGHT)
                                    .build();
                        } else {
                            return p;
                        }
                    })
                    .toList();
            game = game
                    .toBuilder()
                    .clearParticipants()
                    .addAllParticipants(newParticipants)
                    .build();
        }
        GameOuterClass.Cycle historyItem = game
                .getHistory(game.getHistoryCount() - 1)
                .toBuilder()
                .setNight(game.getNight())
                .build();
        return game
                .toBuilder()
                .setHistory(game.getHistoryCount() - 1, historyItem)
                .setDay(GameOuterClass.DayState.newBuilder().build())
                .setCycleNumber(game.getCycleNumber() + 1)
                .build();
    }
}
