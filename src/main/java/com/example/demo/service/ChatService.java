package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

@Service
public class ChatService {
    public record Message(
            String body,
            String author
    ) {}

    private static class Chat {
        private boolean closed = false;
        private final Map<String, LinkedBlockingQueue<Message>> users = new HashMap<>();
    }

    private static final Message CLOSE = new Message("Chat was closed", "<system>");

    ConcurrentHashMap<String, Chat> chats = new ConcurrentHashMap<>();

    private Chat getChat(String room) {
        return chats.computeIfAbsent(room, (k) -> new Chat());
    }

    public void send(String room, Message message) {
        Chat chat = getChat(room);
        synchronized (chat) {
            if (chat.closed) {
                return;
            }
            chat.users.forEach((id, queue) -> {
                if (!id.equals(message.author)) {
                    queue.add(message);
                }
            });
        }
    }

    public void subscribe(String room, String id, Function<Message, Boolean> consumer)
            throws AlreadySubscribedException, InterruptedException {
        Chat chat = getChat(room);
        var queue = new LinkedBlockingQueue<Message>();
        synchronized (chat) {
            if (chat.closed) {
                return;
            }
            if (chat.users.containsKey(id)) {
                throw new AlreadySubscribedException();
            }
            chat.users.put(id, queue);
        }
        while (true) {
            Message m = queue.take();
            if (m == CLOSE) {
                break;
            }
            boolean cont = consumer.apply(m);
            if (!cont) {
                break;
            }
        }
        synchronized (chat) {
            chat.users.remove(id);
        }
    }

    public void close(String room) {
        Chat chat = getChat(room);
        synchronized (chat) {
            chat.closed = true;
            chat.users.forEach((id, queue) -> queue.add(CLOSE));
        }

    }

    public static class AlreadySubscribedException extends Exception {
    }
}
