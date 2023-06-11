package com.example.demo.rest;

import com.example.demo.model.User;
import com.example.demo.repo.UserRepository;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UsersEndpoint {
    private final UserRepository repository;

    @Autowired
    UsersEndpoint(
            UserRepository repository
    ) {
        this.repository = repository;
    }

    @GetMapping("/{login}")
    @Nullable
    public User get(@PathVariable("login") String login) {
        User u = repository.find(login);
        if (u != null) {
            u = u.stripPassword();
        }
        return u;
    }

    @GetMapping
    public List<User> getAll() {
        return repository.findAll();
    }

    @PutMapping("/{login}")
    public ResponseEntity<Void> put(@PathVariable("login") String login, @RequestBody User user) {
        if (!user.login().equals(login)) {
            return ResponseEntity.badRequest().build();
        }
        repository.save(user);
        return ResponseEntity.ofNullable(null);
    }
}
