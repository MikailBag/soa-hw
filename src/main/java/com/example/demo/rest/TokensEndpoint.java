package com.example.demo.rest;

import com.example.demo.model.User;
import com.example.demo.repo.UserRepository;
import com.example.demo.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tokens")
public class TokensEndpoint {
    private final UserRepository users;
    private final TokenService tokens;

    @Autowired
    TokensEndpoint(
            UserRepository users,
            TokenService tokens
    ) {
        this.users = users;
        this.tokens = tokens;
    }

    public record LoginRequest(
            String username,
            String password
    ) {
    }

    public record LoginResponse(
            String token,
            String error
    ) {
    }

    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = users.find(request.username());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new LoginResponse("", "user not found"));
        }
        // TODO: insecure comparison
        if (!user.password().equals(request.password())) {
            return ResponseEntity.badRequest().build();
        }
        String token = tokens.makeToken(request.username);
        return ResponseEntity.ok(new LoginResponse(token, ""));
    }
}
