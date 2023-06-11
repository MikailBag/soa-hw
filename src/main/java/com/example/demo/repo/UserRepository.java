package com.example.demo.repo;

import com.example.demo.model.User;
import jakarta.annotation.Nullable;

import java.util.List;

public interface UserRepository {
    class InvalidStateException extends Exception {
        InvalidStateException(String message) {
            super(message);
        }
    }
    void save(User user);
    @Nullable
    User find(String login);
    List<User> findAll();
}
