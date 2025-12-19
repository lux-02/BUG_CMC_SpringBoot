package com.example.board.exception;
public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Long postId) {
        super("Post not found: " + postId);
    }
}
