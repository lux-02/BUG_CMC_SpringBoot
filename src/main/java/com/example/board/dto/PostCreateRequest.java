package com.example.board.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PostCreateRequest {
    private Long categoryId;
    private String title;
    private String content;

    public static PostCreateRequest of(Long categoryId, String title, String content) {
        return PostCreateRequest.builder()
                .categoryId(categoryId)
                .title(title)
                .content(content)
                .build();
    }
}

