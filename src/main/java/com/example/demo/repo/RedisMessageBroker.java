package com.example.demo.repo;

import com.example.demo.api.chat.Chat;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Repository
@ConditionalOnProperty("messaging.redis.enabled")
public class RedisMessageBroker implements MessageBroker, AutoCloseable {
    private static class Subscriber extends BinaryJedisPubSub {
        private final Logger log = LoggerFactory.getLogger(Subscriber.class);
        private final SubscriberNotifier notifier;
        private final CompletableFuture<Void> future;

        Subscriber(SubscriberNotifier notifier, CompletableFuture<Void> future) {
            this.notifier = notifier;
            this.future = future;
        }

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            try {
                var dto = Chat.ServerInternalMessageDto.parseFrom(message);
                notifier.deliver(dto.getTopic(), dto.getData());
            } catch (InvalidProtocolBufferException ex) {
                log.error("failed to parse received message", ex);
            } catch (InterruptedException ex) {
                log.error("unexpected interrupt", ex);
            }
        }

        @Override
        public void onSubscribe(byte[] channel, int subscribedChannels) {
            if (future.isDone()) {
                log.error("onSubscribe called more than one time");
                return;
            }
            future.complete(null);
        }
    }

    private final Logger log = LoggerFactory.getLogger(RedisMessageBroker.class);
    private final SubscriberNotifier notifier;
    private final JedisPooled jedis;
    private final String key;
    private final Subscriber subscriber;

    @Autowired
    RedisMessageBroker(
            @Value("${messaging.redis.buffer-size:256}") int bufferSize,
            @Value("${messaging.redis.host:127.0.0.1}") String redisHost,
            @Value("${messaging.redis.port:6379}") int redisPort,
            @Value("${messaging.redis.key:MAFIA}") String key
    ) {
        this.notifier = new SubscriberNotifier(bufferSize);
        this.jedis = new JedisPooled(redisHost, redisPort);
        this.key = key;
        var f = new CompletableFuture<Void>();
        this.subscriber = new Subscriber(notifier, f);
        Thread.ofVirtual()
                .name("RedisMessageBroker-SubscribingThread")
                .start(() -> {
                    try {
                        jedis.subscribe(subscriber, key.getBytes(StandardCharsets.UTF_8));
                    } catch (Throwable ex) {
                        f.completeExceptionally(ex);
                        throw ex;
                    }
                });
        f.join();
    }


    @Override
    public void publish(Chat.Topic topic, Chat.MessageData message) throws IOException {
        var dto = Chat.ServerInternalMessageDto.newBuilder()
                .setData(message)
                .setTopic(topic)
                .build();
        try {
            jedis.publish(key.getBytes(StandardCharsets.UTF_8), dto.toByteArray());
        } catch (JedisException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Subscription subscribe(BiConsumer<Chat.Topic, Chat.MessageData> consumer) {
        return notifier.subscribe(consumer);
    }

    @Override
    public void close() {
        try {
            subscriber.unsubscribe();
        } catch (JedisException ex) {
            log.error("failed to unsubscribe", ex);
        }
        notifier.close();
        jedis.close();
    }
}
