package com.example.demo.repo;

import com.example.demo.model.User;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Repository
class MemoryUserRepository implements UserRepository {
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    @Autowired
    MemoryUserRepository() {
        save(new User("client1", "password1", new byte[0], false, "1@localhost"));
        save(new User("client2", "password2", new byte[0], true, "2@localhost"));
        save(new User("client3", "password3", new byte[0], false, "3@localhost"));
        save(new User("client4", "password4", new byte[0], true, "4@localhost"));
    }

    @Override
    public void save(User user) {
        users.put(user.login(), user);
    }

    @Nullable
    @Override
    public User find(String login) {
        return users.get(login);
    }

    @Override
    public List<User> findAll() {
        // TODO: not a snapshot read
        return new ArrayList<>(users.values());
    }
}
