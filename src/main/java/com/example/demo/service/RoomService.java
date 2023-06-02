package com.example.demo.service;

import com.example.demo.api.game.GameOuterClass;
import com.example.demo.repo.GameRepository;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class RoomService {
    private final GameRepository repository;

    @Autowired
    RoomService(
            GameRepository repository
    ) {
        this.repository = repository;
    }

    public static class InvalidSettingsException extends Exception {
        private final String description;

        InvalidSettingsException(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public GameOuterClass.Game create(GameOuterClass.Settings settings, String title) throws InvalidSettingsException {
        if (settings.getPlayerCount() <= 2) {
            throw new InvalidSettingsException("It does not make sense to have less than 3 players");
        }
        if (settings.getCriminalCount() <= 0) {
            throw new InvalidSettingsException("There must be at least once criminal");
        }
        if (settings.getPolicemanCount() < 0) {
            throw new InvalidSettingsException("Policeman count may not be negative");
        }
        if (settings.getPolicemanCount() + settings.getCriminalCount() > settings.getPlayerCount()) {
            throw new InvalidSettingsException("Too many special roles");
        }
        var game = GameOuterClass.Game.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setState(GameOuterClass.Game.State.NOT_STARTED)
                .setSettings(settings)
                .setTitle(title)
                .build();

        try {
            game = repository.put(game);
        } catch (GameRepository.ConflictException ex) {
            // should be unreachable since ids are random
            throw new RuntimeException(ex);
        }
        return game;
    }

    public static class GameAlreadyStartedException extends Exception {
    }

    public static class NameAlreadyUsedException extends Exception {
    }

    private List<GameOuterClass.Participant> generateRoles(
            List<GameOuterClass.Participant> participants, GameOuterClass.Settings settings
    ) {
        boolean ok = participants
                .stream()
                .allMatch(p -> p.getRole().equals(GameOuterClass.Role.UNKNOWN));
        if (!ok) {
            throw new IllegalArgumentException("roles already assigned");
        }
        if (participants.size() != settings.getPlayerCount()) {
            throw new IllegalArgumentException("incorrect participant count");
        }
        var roles = new ArrayList<GameOuterClass.Role>();
        roles.add(GameOuterClass.Role.CRIMINAL_BOSS);
        for (int i = 1; i < settings.getCriminalCount(); ++i) {
            roles.add(GameOuterClass.Role.CRIMINAL);
        }
        for (int i = 0; i < settings.getPolicemanCount(); ++i) {
            roles.add(GameOuterClass.Role.POLICEMAN);
        }
        while (roles.size() < settings.getPlayerCount()) {
            roles.add(GameOuterClass.Role.NORMAL);
        }
        Collections.shuffle(roles);
        var output = new ArrayList<GameOuterClass.Participant>();
        for (int i = 0; i < settings.getPlayerCount(); ++i) {
            var b = participants.get(i).toBuilder();
            b.setRole(roles.get(i));
            output.add(b.build());
        }
        return output;
    }

    @Nullable
    public GameOuterClass.Game get(String id) {
        return repository.get(id);
    }

    public GameOuterClass.Participant join(
            String id, String name
    ) throws GameRepository.ConflictException, GameAlreadyStartedException, UnknownGameException,
            NameAlreadyUsedException {
        GameOuterClass.Game game = repository.get(id);
        if (game == null) {
            throw new UnknownGameException();
        }
        switch (game.getState()) {
            case FINISHED, IN_PROGRESS -> throw new GameAlreadyStartedException();
        }
        boolean nameAlreadyUsed = game
                .getParticipantsList()
                .stream()
                .anyMatch(p -> p.getName().equals(name));
        if (nameAlreadyUsed) {
            throw new NameAlreadyUsedException();
        }
        String participantId = UUID.randomUUID().toString();
        GameOuterClass.Participant newParticipant = GameOuterClass.Participant.newBuilder()
                .setName(name)
                .setId(participantId)
                .setName(name)
                .build();
        game = game.toBuilder()
                .addParticipants(newParticipant).build();
        int newParticipantCount = game.getParticipantsCount();
        if (newParticipantCount == game.getSettings().getPlayerCount()) {
            var nightState = GameOuterClass.NightState.newBuilder()
                    .build();
            var participants = generateRoles(game.getParticipantsList(), game.getSettings());

            game = game
                    .toBuilder()
                    .setNight(nightState)
                    .addHistory(GameOuterClass.Cycle.newBuilder().build())
                    .setCycleNumber(1)
                    .setState(GameOuterClass.Game.State.IN_PROGRESS)
                    .clearParticipants()
                    .addAllParticipants(participants)
                    .build();
        }
        repository.put(game);
        return newParticipant;
    }

    public List<GameOuterClass.Game> list() {
        return repository.list().stream().toList();
    }
}
