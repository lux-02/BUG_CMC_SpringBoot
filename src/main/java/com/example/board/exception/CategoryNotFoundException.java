package com.example.board.exception;
public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long categoryId) {
        super("Category not found: " + categoryId);
    }
}
