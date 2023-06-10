package com.example.demo.service;

import com.example.demo.api.chat.Chat;
import com.example.demo.api.game.GameOuterClass;
import com.example.demo.repo.GameRepository;
import com.example.demo.repo.GameWatcher;
import com.example.demo.repo.MessageBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Service
public class ChatService implements BiConsumer<Chat.Topic, Chat.MessageData>, GameWatcher, AutoCloseable {
    private final Logger log = LoggerFactory.getLogger(ChatService.class);

    public static class PermissionDeniedException extends Exception {
        PermissionDeniedException(String message) {
            super(message);
        }
    }

    private static class TopicState {
        private final Lock lock = new ReentrantLock();
        private boolean closed = false;
        private final HashMap<UUID, BlockingQueue<Chat.MessageData>> users = new HashMap<>();
    }

    private static final String SYSTEM = "system";

    private static final Chat.MessageData CLOSE = Chat.MessageData.newBuilder()
            .setBody("Chat was closed")
            .setParticipantId(SYSTEM)
            .setEof(true)
            .build();

    private final MessageBroker broker;
    private final MessageBroker.Subscription subscription;
    private final GameRepository repository;

    private final ConcurrentHashMap<String, List<String>> gameToTopicsMapping = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TopicState> topics = new ConcurrentHashMap<>();
    private final Lock watcherLock = new ReentrantLock();
    private final Lock activeGamesLock = new ReentrantLock();
    private final Set<String> activeGames = new HashSet<>();

    @Autowired
    ChatService(
            MessageBroker broker,
            GameRepository repository
    ) {
        this.broker = broker;
        this.subscription = broker.subscribe(this);
        this.repository = repository;
        this.repository.watch(this);
    }

    private TopicState getTopicState(Chat.Topic topic) {
        String key = topic.getRoomId() + "/" + topic.getStreamId();
        if (topics.containsKey(key)) {
            return topics.get(key);
        }

        activeGamesLock.lock();
        try {
            if (!activeGames.contains(topic.getRoomId())) {
                TopicState dummy = new TopicState();
                dummy.closed = true;
                return dummy;
            }
            gameToTopicsMapping.computeIfAbsent(topic.getRoomId(), (k) -> new ArrayList<>()).add(key);
            return topics.computeIfAbsent(key, (k) -> new TopicState());
        } finally {
            activeGamesLock.unlock();
        }
    }

    private void checkAccess(Chat.Topic topic, String id) throws PermissionDeniedException {
        log.info("checking access");
        GameOuterClass.Game game = repository.get(topic.getRoomId());
        if (game == null) {
            log.info("rejecting access: game does not exist");
            throw new PermissionDeniedException("game " + topic.getRoomId() + " does not exist");
        }
        if (!topic.getStreamId().equals("main") && !game.getState().equals(GameOuterClass.Game.State.IN_PROGRESS)) {
            log.info("rejecting access: invalid game state");
            throw new PermissionDeniedException("Non-main streams are only available for in-progress games");
        }
        log.info("Searching for participant");
        GameOuterClass.Participant participant = game
                .getParticipantsList()
                .stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new PermissionDeniedException("Only game participants can use chat"));
        log.info("Proceeding with participant");
        switch (topic.getStreamId()) {
            case "main" -> {
            }
            case "criminals" -> {
                Set<GameOuterClass.Role> criminalRoles = Set.of(
                        GameOuterClass.Role.CRIMINAL, GameOuterClass.Role.CRIMINAL_BOSS
                );
                if (!criminalRoles.contains(participant.getRole())) {
                    throw new PermissionDeniedException("Criminal chat is available only for criminals");
                }
            }
            case "dead" -> {
                Set<GameOuterClass.Participant.State> deadStates = Set.of(
                        GameOuterClass.Participant.State.KILLED_AT_DAY,
                        GameOuterClass.Participant.State.KILLED_AT_NIGHT
                );
                if (!deadStates.contains(participant.getState())) {
                    throw new PermissionDeniedException("Dead chat is not for alive");
                }
            }
            default -> throw new PermissionDeniedException("Unknown stream");
        }
        log.info("granting access");
    }

    public void send(Chat.Topic topic, Chat.MessageData message) throws IOException, PermissionDeniedException {
        checkAccess(topic, message.getParticipantId());
        broker.publish(topic, message);
    }

    public void sendSystemMessage(String game, String message) {
        var topic = Chat.Topic
                .newBuilder()
                .setRoomId(game)
                .setStreamId("main")
                .build();

        var m = Chat.MessageData
                .newBuilder()
                .setBody(message)
                .setParticipantId(SYSTEM)
                .build();
        try {
            broker.publish(topic, m);
        } catch (IOException ex) {
            log.warn("failed to send system message {}", message, ex);
        }
    }

    @Override
    public void accept(Chat.Topic topic, Chat.MessageData messageData) {
        TopicState state = getTopicState(topic);
        state.lock.lock();
        try {
            state.users.forEach((id, queue) -> queue.add(messageData));
            log.info("Delivered message to {} watches", state.users.size());
        } finally {
            state.lock.unlock();
        }
    }

    private Runnable subscribeQueue(BlockingQueue<Chat.MessageData> queue, Chat.Topic topic) {
        TopicState state = getTopicState(topic);
        UUID uuid = UUID.randomUUID();
        state.lock.lock();
        try {
            if (state.closed) {
                log.info("terminating subscription: topic is closed");
                boolean ignored = queue.offer(CLOSE);
                return () -> {
                };
            }
            log.info("created subscription to {}/{}", topic.getRoomId(), topic.getStreamId());
            state.users.put(uuid, queue);
        } finally {
            state.lock.unlock();
        }
        return () -> {
            state.lock.lock();
            try {
                if (state.closed) {
                    return;
                }
                state.users.remove(uuid);
            } finally {
                state.lock.unlock();
            }
            log.info("removed subscription to {}/{}", topic.getRoomId(), topic.getStreamId());
        };
    }

    public void subscribe(
            Chat.Topic topic, Function<Chat.MessageData, Boolean> consumer, String id
    ) throws InterruptedException, PermissionDeniedException {
        log.info("Processing subscribe request for {}/{}", topic.getRoomId(), topic.getStreamId());
        checkAccess(topic, id);
        log.info("Access was granted, proceeding");
        var queue = new LinkedBlockingQueue<Chat.MessageData>();
        Runnable cleanup = subscribeQueue(queue, topic);
        try {
            while (true) {
                Chat.MessageData m = queue.take();
                if (m.getEof()) {
                    break;
                }
                boolean cont = consumer.apply(m);
                if (!cont) {
                    break;
                }
            }
        } finally {
            cleanup.run();
        }
    }

    @Override
    public void onApply(GameOuterClass.Game game) {
        log.info("Processing game change");
        // TODO: list all games on startup
        boolean shouldBeActive = Set.of(
                GameOuterClass.Game.State.IN_PROGRESS,
                GameOuterClass.Game.State.NOT_STARTED
        ).contains(game.getState());
        boolean needsToClearGame = false;
        watcherLock.lock();
        try {
            boolean isActive = activeGames.contains(game.getId());
            if (isActive == shouldBeActive) {
                return;
            }
            activeGamesLock.lock();
            try {
                if (shouldBeActive) {
                    log.info("Marking game as active");
                    activeGames.add(game.getId());
                } else {
                    log.info("Marking game as inactive");
                    activeGames.remove(game.getId());
                    needsToClearGame = true;
                }
            } finally {
                activeGamesLock.unlock();
            }
        } finally {
            watcherLock.unlock();
        }
        if (!needsToClearGame) {
            return;
        }
        List<String> topicsToCleanup = gameToTopicsMapping.remove(game.getId());
        if (topicsToCleanup == null) {
            return;
        }
        log.info("Closing expired topics {}", topicsToCleanup);
        for (String k : topicsToCleanup) {
            TopicState state = topics.get(k);
            if (state == null) {
                continue;
            }
            state.lock.lock();
            try {
                state.closed = true;
                state.users.forEach((id, user) -> {
                    boolean ignored = user.offer(CLOSE);
                });
                state.users.clear();
            } finally {
                state.lock.unlock();
            }
        }
    }

    @Override
    public void close() {
        boolean interrupted = false;
        log.info("unsubscribing from broker");
        while (true) {
            try {
                subscription.unsubscribe();
                log.info("success");
                break;
            } catch (InterruptedException ex) {
                log.warn("interrupted at shutdown");
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
