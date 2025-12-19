package com.example.board.service;

import com.example.board.domain.Role;
import com.example.board.domain.User;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 서비스
 * 개선 사항:
 * 1. Builder 패턴 사용
 * 2. 명확한 트랜잭션 범위
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long join(String email, String password, Role role) {
        String encodedPassword = passwordEncoder.encode(password);

        // Builder 패턴 사용
        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .role(role)
                .build();

        userRepository.save(user);
        return user.getId();
    }

    @Transactional(readOnly = true)
    public List<User> findUsers() {
        return userRepository.findAll();
    }
}