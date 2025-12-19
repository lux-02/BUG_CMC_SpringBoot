package com.example.board.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDto {
    private Long userId;      // 테스트용 (실제론 로그인 정보로 덮어씌움)
    private Long categoryId;
    private String title;
    private String content;
}