package com.example.demo.ctl;

import com.example.demo.api.game.GameOuterClass;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class CriminalVoteController implements Controller {
    @Nullable
    @Override
    public GameOuterClass.Game reconcile(GameOuterClass.Game game) {
        if (!game.hasNight()) {
            return null;
        }
        if (game.getNight().getCriminalVoteComplete()) {
            return null;
        }
        var roles = Set.of(GameOuterClass.Role.CRIMINAL, GameOuterClass.Role.CRIMINAL_BOSS);
        long aliveCriminals = game
                .getParticipantsList()
                .stream()
                .filter(
                        p ->
                                roles.contains(p.getRole())
                                        && p.getState().equals(GameOuterClass.Participant.State.ALIVE)
                )
                .count();
        if (game.getNight().getCriminalVotesCount() != aliveCriminals) {
            return null;
        }

        List<String> winners = VoteUtil.getWinners(game.getNight().getCriminalVotesList());
        String chosen = null;
        if (winners.size() == 1) {
            chosen = winners.get(0);
        } else if (game.getSettings().getCriminalBossBreaksTies()) {
            chosen = winners.get(ThreadLocalRandom.current().nextInt(winners.size()));
        } else {
            // tie
            chosen = null;
        }
        GameOuterClass.NightState newNightState = game.getNight()
                .toBuilder()
                .setCriminalVoteComplete(true)
                .build();
        if (chosen != null) {
            newNightState = newNightState.toBuilder()
                    .setVictimId(chosen)
                    .build();
        }


        return game
                .toBuilder()
                .setNight(newNightState)
                .build();
    }
}
