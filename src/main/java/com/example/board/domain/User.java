package com.example.board.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users") // DB 예약어 피하기 위해 테이블명을 users로 지정
public class User {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;

    @Enumerated(EnumType.STRING) // Enum을 문자로 DB에 저장 (USER, ADMIN)
    private Role role;

    private LocalDateTime createdAt;

    // 1. JPA용 기본 생성자 (막아둠)
    public User() {}

    // 2. [추가] 테스트나 실제 로직에서 쓸 '전체 생성자' (열어둠)s
    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    // Getter (값 꺼내기용)
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public String getPassword() {
        return password;
    }
}