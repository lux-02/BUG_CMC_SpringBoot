package com.example.board.dto;

public class CommentCreateRequest {
    private Long userId;
    private Long postId;
    private Long parentId; // 대댓글이 아니면 null이 들어옴
    private String content;

    public CommentCreateRequest() {}

    public CommentCreateRequest(Long userId, Long postId, Long parentId, String content) {
        this.userId = userId;
        this.postId = postId;
        this.parentId = parentId;
        this.content = content;
    }

    public Long getUserId() { return userId; }
    public Long getPostId() { return postId; }
    public Long getParentId() { return parentId; }
    public String getContent() { return content; }
}