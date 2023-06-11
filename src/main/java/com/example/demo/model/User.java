package com.example.demo.model;

public record User(
        String login,
        // TODO: secure storage
        String password,
        byte[] picture,
        // irony / post-irony / meta-irony :shrug:
        boolean gender,
        String email
) {
    public User stripPassword() {
        return new User(login, "", picture, gender, email);
    }
}
