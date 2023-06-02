package com.example.demo.ctl;

import com.example.demo.api.game.GameOuterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
class CitizenVoteController implements Controller {
    private static final Logger log = LoggerFactory.getLogger(CitizenVoteController.class);

    @Autowired
    CitizenVoteController() {
    }


    private GameOuterClass.Game finalizeCitizenVote(GameOuterClass.Game game) {
        List<String> elected = VoteUtil.getWinners(
                game
                        .getDay()
                        .getCitizenVotesList()
        );
        if (elected.size() > 1) {
            log.info("tie, nobody dies");
            return game;
        }
        String chosen = elected.get(0);
        log.info("{} dies", chosen);
        List<GameOuterClass.Participant> participants = game
                .getParticipantsList()
                .stream()
                .map(participant -> {
                    if (participant.getId().equals(chosen)) {
                        return participant
                                .toBuilder()
                                .setState(GameOuterClass.Participant.State.KILLED_AT_DAY)
                                .build();
                    } else {
                        return participant;
                    }
                })
                .toList();
        return game
                .toBuilder()
                .clearParticipants()
                .addAllParticipants(participants)
                .build();
    }

    private GameOuterClass.Game completeDay(GameOuterClass.Game game) {
        var criminalRoles = Set.of(
                GameOuterClass.Role.CRIMINAL,
                GameOuterClass.Role.CRIMINAL_BOSS
        );
        boolean criminalsRemain = game
                .getParticipantsList()
                .stream()
                .filter(p -> p.getState().equals(GameOuterClass.Participant.State.ALIVE))
                .anyMatch(p -> criminalRoles.contains(p.getRole()));
        if (!criminalsRemain) {
            return game
                    .toBuilder()
                    .setState(GameOuterClass.Game.State.FINISHED)
                    .setOutcome(GameOuterClass.Game.Outcome.CITIZENS_WON)
                    .build();
        }
        var historyItem = GameOuterClass.Cycle.newBuilder()
                .setDay(game.getDay());
        // night begins
        var nightState = GameOuterClass.NightState.newBuilder()
                .build();
        return game
                .toBuilder()
                .setNight(nightState)
                .addHistory(historyItem)
                .build();
    }

    @Override
    public GameOuterClass.Game reconcile(GameOuterClass.Game game) {
        if (!game.hasDay()) {
            return null;
        }
        if (!game.getState().equals(GameOuterClass.Game.State.IN_PROGRESS)) {
            return null;
        }
        long expected = game
                .getParticipantsList()
                .stream()
                .filter(p -> p.getState().equals(GameOuterClass.Participant.State.ALIVE))
                .count();
        long diff = expected - game.getDay().getCitizenVotesCount();
        if (diff < 0) {
            log.info("game is {}", game);
            long actual = game.getDay().getCitizenVotesCount();
            throw new IllegalStateException("too many citizen votes: expected " + expected + " but got " + actual);
        }
        if (diff == 0) {
            game = finalizeCitizenVote(game);
            return completeDay(game);
        }
        log.info("Waiting for {} votes", diff);
        return null;
    }
}
