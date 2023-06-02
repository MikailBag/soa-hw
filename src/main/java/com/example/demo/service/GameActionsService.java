package com.example.demo.service;

import com.example.demo.api.game.GameOuterClass;
import com.example.demo.ctl.ControllerManager;
import com.example.demo.repo.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class GameActionsService {
    private final GameRepository repository;

    @Autowired
    GameActionsService(GameRepository repository) {
        this.repository = repository;
    }

    public abstract static sealed class ActionException extends Exception
            permits ActionNotMatchesRoleException, ActionNotAvailableException, ActionAlreadyUsedException {
    }

    public static final class ActionNotMatchesRoleException extends ActionException {
    }

    public static final class ActionNotAvailableException extends ActionException {
    }

    public static final class ActionAlreadyUsedException extends ActionException {
    }

    public static class InvalidTargetException extends Exception {
        InvalidTargetException(String message) {
            super(message);
        }
    }

    private void validateParticipantId(GameOuterClass.Game game, String participantId) throws InvalidTargetException {
        Optional<GameOuterClass.Participant> participant = game
                .getParticipantsList()
                .stream()
                .filter(p -> p.getId().equals(participantId))
                .findAny();
        if (participant.isEmpty()) {
            throw new InvalidTargetException("There is no participant " + participantId + " in this game");
        }
        if (!participant.get().getState().equals(GameOuterClass.Participant.State.ALIVE)) {
            throw new InvalidTargetException("Participant is not alive: " + participant.get().getState());
        }
    }

    private void validateRole(
            GameOuterClass.Game game, String participantId, Set<GameOuterClass.Role> roles
    ) throws ActionNotMatchesRoleException {
        boolean ok = game
                .getParticipantsList()
                .stream()
                .anyMatch(p -> p.getId().equals(participantId) && roles.contains(p.getRole()));
        if (!ok) {
            throw new ActionNotMatchesRoleException();
        }
    }

    public void processCitizenVote(
            String id, String voter, String target
    ) throws UnknownGameException, ActionException, InvalidTargetException,
            GameRepository.ConflictException {
        var game = repository.get(id);
        if (game == null) {
            throw new UnknownGameException();
        }
        // citizen may vote for himself - why not?
        validateParticipantId(game, voter);
        validateParticipantId(game, target);
        if (!game.hasDay()) {
            throw new ActionNotAvailableException();
        }
        boolean alreadyVoted = game
                .getDay()
                .getCitizenVotesList()
                .stream()
                .anyMatch(v -> v.getVoterId().equals(voter));
        if (alreadyVoted) {
            throw new ActionAlreadyUsedException();
        }

        var vote = GameOuterClass.Vote.newBuilder()
                .setVoterId(voter)
                .setTargetId(target);
        GameOuterClass.DayState newDayState = game.getDay().toBuilder().addCitizenVotes(vote).build();
        game = game.toBuilder()
                .setDay(newDayState)
                .build();
        repository.put(game);
    }

    public void processCriminalVote(
            String id, String voter, String target
    ) throws UnknownGameException, GameRepository.ConflictException, ActionException, InvalidTargetException {
        var game = repository.get(id);
        if (game == null) {
            throw new UnknownGameException();
        }
        // criminal may vote for himself or another criminal - why not, may be a smart move
        validateParticipantId(game, voter);
        validateParticipantId(game, target);
        validateRole(game, voter, Set.of(GameOuterClass.Role.CRIMINAL, GameOuterClass.Role.CRIMINAL_BOSS));
        if (!game.hasNight()) {
            throw new ActionNotAvailableException();
        }

        boolean alreadyVoted = game
                .getNight()
                .getCriminalVotesList()
                .stream()
                .anyMatch(v -> v.getVoterId().equals(voter));
        if (alreadyVoted) {
            throw new ActionAlreadyUsedException();
        }

        var vote = GameOuterClass.Vote.newBuilder()
                .setVoterId(voter)
                .setTargetId(target)
                .build();
        GameOuterClass.NightState newNightState = game
                .getNight()
                .toBuilder()
                .addCriminalVotes(vote)
                .build();
        game = game
                .toBuilder()
                .setNight(newNightState)
                .build();
        repository.put(game);
    }

    public void processPolicemanCheck(
            String id, String policeman, String suspect
    ) throws UnknownGameException, InvalidTargetException, ActionException, GameRepository.ConflictException {
        var game = repository.get(id);
        if (game == null) {
            throw new UnknownGameException();
        }
        // policeman can check himself - stupid but allowed
        validateParticipantId(game, policeman);
        validateParticipantId(game, suspect);
        validateRole(game, policeman, Set.of(GameOuterClass.Role.POLICEMAN));
        if (!game.hasNight()) {
            throw new ActionNotAvailableException();
        }

        boolean alreadyChecked = game
                .getNight()
                .getPolicemanChecksList()
                .stream()
                .anyMatch(v -> v.getPolicemanId().equals(policeman));
        if (alreadyChecked) {
            throw new ActionAlreadyUsedException();
        }

        var check = GameOuterClass.PolicemanCheck.newBuilder()
                .setPolicemanId(policeman)
                .setTargetId(suspect)
                .build();
        GameOuterClass.NightState newNightState = game
                .getNight()
                .toBuilder()
                .addPolicemanChecks(check)
                .build();
        game = game
                .toBuilder()
                .setNight(newNightState)
                .build();
        repository.put(game);
    }
}
