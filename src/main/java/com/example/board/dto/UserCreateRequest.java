package com.example.board.dto;

import com.example.board.domain.Role;

public class UserCreateRequest {
    private String email;
    private String password;
    private Role role; // "USER" 또는 "ADMIN"이라고 보내면 알아서 변환됨

    // 기본 생성자 (필수)
    public UserCreateRequest() {}

    public UserCreateRequest(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getter
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
}