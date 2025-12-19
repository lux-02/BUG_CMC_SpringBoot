package com.example.board.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 예: 자유게시판, 질문게시판

    // [추가] 테스트 코드에서 "new Category()"를 쓰기 위해 필요합니다.
    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}