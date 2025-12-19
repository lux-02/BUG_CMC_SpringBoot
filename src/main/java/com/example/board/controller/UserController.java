package com.example.board.controller;

import com.example.board.dto.UserCreateRequest;
import com.example.board.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // [회원가입] POST http://localhost:8080/users/signup
    @PostMapping("/users/signup")
    public String signup(@RequestBody UserCreateRequest request) {
        Long userId = userService.join(
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );
        return "회원가입 성공! 유저 ID: " + userId;
    }
}