package com.example.board.exception;
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("User not found: " + userId);
    }
    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }
}
