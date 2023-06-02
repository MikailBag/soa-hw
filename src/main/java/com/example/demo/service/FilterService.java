package com.example.demo.service;

import com.example.demo.api.game.GameOuterClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilterService {
    @Autowired
    FilterService() {
    }

    private GameOuterClass.Participant find(GameOuterClass.Game game, String id) {
        return game
                .getParticipantsList()
                .stream()
                .filter(p -> p.getId().equals(id))
                .findAny()
                .orElseThrow(IllegalStateException::new);
    }

    private boolean canSee(GameOuterClass.Game game, String whoId, String targetId) {
        if (whoId.isEmpty()) {
            return false;
        }
        if (whoId.equals(targetId)) {
            return true;
        }
        GameOuterClass.Participant who = find(game, whoId);
        GameOuterClass.Participant target = find(game, targetId);
        if (game.getSettings().getDeadKnowEverything()) {
            if (!who.getState().equals(GameOuterClass.Participant.State.ALIVE)) {
                return true;
            }
        }
        if (game.getSettings().getKilledAreRevealed()) {
            if (target.getState().equals(GameOuterClass.Participant.State.KILLED_AT_NIGHT)) {
                return true;
            }
        }
        if (game.getSettings().getExecutedAreRevealed()) {
            if (target.getState().equals(GameOuterClass.Participant.State.KILLED_AT_DAY)) {
                return true;
            }
        }
        Set<GameOuterClass.Role> criminalRoles = Set.of(
                GameOuterClass.Role.CRIMINAL, GameOuterClass.Role.CRIMINAL_BOSS
        );
        if (criminalRoles.contains(who.getRole()) && criminalRoles.contains(target.getRole())) {
            return true;
        }
        return game
                .getHistoryList()
                .stream()
                .flatMap(item -> item.getNight().getPolicemanChecksList().stream())
                .filter(pc -> pc.getPolicemanId().equals(whoId) || pc.getRevealed())
                .anyMatch(pc -> pc.getTargetId().equals(targetId));
    }

    private GameOuterClass.Game stripPrivateFields(GameOuterClass.Game game) {
        if (game.hasNight()) {
            game = game
                    .toBuilder()
                    .setNight(GameOuterClass.NightState.newBuilder().build())
                    .build();
        }
        return game
                .toBuilder()
                .clearHistory()
                .build();
    }

    public GameOuterClass.Game filter(GameOuterClass.Game game, String pointOfView) {
        if (!game.getState().equals(GameOuterClass.Game.State.IN_PROGRESS)) {
            return game;
        }
        List<GameOuterClass.Participant> filteredParticipants = game
                .getParticipantsList()
                .stream()
                .map(p -> {
                    boolean roleVisible = canSee(game, pointOfView, p.getId());
                    if (!roleVisible) {
                        p = p.toBuilder().setRole(GameOuterClass.Role.UNKNOWN).build();
                    }
                    return p;
                })
                .toList();
        return stripPrivateFields(game)
                .toBuilder()
                .clearParticipants()
                .addAllParticipants(filteredParticipants)
                .build();
    }
}
