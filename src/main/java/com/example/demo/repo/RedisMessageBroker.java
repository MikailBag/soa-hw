package com.example.demo.repo;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Repository
@ConditionalOnProperty("messaging.redis.enabled")
public class RedisMessageBroker implements MessageBroker, AutoCloseable {
    private interface SubscriberBase {
        void unsubscribe();
    }

    private static class PubSubSubscriber<T> extends BinaryJedisPubSub implements SubscriberBase {
        private final Logger log = LoggerFactory.getLogger(PubSubSubscriber.class);
        private final SubscriberNotifierMap notifier;
        private final CompletableFuture<Void> future;
        private final QueueDescriptor<T> descriptor;

        PubSubSubscriber(QueueDescriptor<T> descriptor, SubscriberNotifierMap notifier, CompletableFuture<Void> future) {
            this.notifier = notifier;
            this.future = future;
            this.descriptor = descriptor;
        }

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            try {
                notifier.deliver(descriptor, descriptor.deserialize(message));
            } catch (IOException ex) {
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
    private final SubscriberNotifierMap notifier;
    private final JedisPooled jedis;
    private final String keyPrefix;
    private final List<SubscriberBase> subscribers;

    @Autowired
    RedisMessageBroker(
            @Value("${messaging.redis.buffer-size:256}") int bufferSize,
            @Value("${messaging.redis.host:127.0.0.1}") String redisHost,
            @Value("${messaging.redis.port:6379}") int redisPort,
            @Value("${messaging.redis.key-prefix:MAFIA}") String keyPrefix
    ) {
        this.notifier = new SubscriberNotifierMap(bufferSize);
        this.jedis = new JedisPooled(redisHost, redisPort);
        this.keyPrefix = keyPrefix;
        this.subscribers = new ArrayList<>();
        start(QueueDescriptors.CHAT_MESSAGES);
        start(QueueDescriptors.REPORT_TASKS);
    }

    private <T> void start(QueueDescriptor<T> descriptor) {
        switch (descriptor.policy()) {
            case WORK_QUEUE -> startPubSub(descriptor);
            case PUB_SUB -> startWorkQueue(descriptor);
        }
    }

    private <T> void startWorkQueue(QueueDescriptor<T> descriptor) {
        byte[] key = (keyPrefix + descriptor.name()).getBytes(StandardCharsets.UTF_8);
        Thread thread = Thread.ofVirtual()
                .name("RedisMessageBroker-SubscribingThread-" + descriptor.name())
                .start(() -> {
                    while (true) {
                        List<byte[]> value;
                        try {
                            value = jedis.brpop(1, key);
                        } catch (JedisException ex) {
                            log.error("failed to receive next value");
                            continue;
                        }
                        if (value.isEmpty()) {
                            log.debug("key {} is empty", descriptor.name());
                            continue;
                        }
                        if (value.size() != 1) {
                            throw new IllegalStateException();
                        }
                        try {
                            notifier.deliver(descriptor, descriptor.deserialize(value.get(0)));
                        } catch (IOException ex) {
                            log.error("failed to parse received message", ex);
                            break;
                        } catch (InterruptedException ex) {
                            log.error("unexpected interrupt", ex);
                            break;
                        }
                    }
                });
        subscribers.add(thread::interrupt);
    }

    private <T> void startPubSub(QueueDescriptor<T> descriptor) {
        var f = new CompletableFuture<Void>();
        var subscriber = new PubSubSubscriber<>(descriptor, notifier, f);
        Thread.ofVirtual()
                .name("RedisMessageBroker-SubscribingThread-" + descriptor.name())
                .start(() -> {
                    try {
                        jedis.subscribe(subscriber, descriptor.name().getBytes(StandardCharsets.UTF_8));
                    } catch (Throwable ex) {
                        f.completeExceptionally(ex);
                        throw ex;
                    }
                });
        f.join();
        subscribers.add(subscriber);
    }


    @Override
    public <T> void publish(QueueDescriptor<T> descriptor, T value) throws IOException {
        try {
            byte[] key = (keyPrefix + descriptor.name()).getBytes(StandardCharsets.UTF_8);
            byte[] val = descriptor.serialize(value);
            switch (descriptor.policy()) {
                case PUB_SUB -> jedis.publish(key, val);
                case WORK_QUEUE -> jedis.lpush(key, val);
            }
        } catch (JedisException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public <T> Subscription subscribe(QueueDescriptor<T> descriptor, Consumer<T> consumer) {
        return notifier.subscribe(descriptor, consumer);
    }

    @Override
    public void close() {
        try {
            subscribers.forEach(SubscriberBase::unsubscribe);
        } catch (JedisException ex) {
            log.error("failed to unsubscribe", ex);
        }
        notifier.close();
        jedis.close();
    }
}
