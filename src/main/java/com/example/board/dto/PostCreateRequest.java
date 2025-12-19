package com.example.board.dto;

public class PostCreateRequest {
    private Long userId;
    private Long categoryId;
    private String title;
    private String content;

    public PostCreateRequest() {}

    public PostCreateRequest(Long userId, Long categoryId, String title, String content) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.content = content;
    }

    public Long getUserId() { return userId; }
    public Long getCategoryId() { return categoryId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
}