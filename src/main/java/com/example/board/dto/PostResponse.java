package com.example.board.dto;

import com.example.board.domain.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String authorEmail;
    private String categoryName;
    private LocalDateTime createdAt;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorEmail(post.getUser().getEmail())
                .categoryName(post.getCategory().getName())
                .createdAt(post.getCreatedAt())
                .build();
    }
}

