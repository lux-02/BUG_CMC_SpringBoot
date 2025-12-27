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
        // [추가] 중복 회원 검증
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        });

        // passwordEncoder.encode(password)로 비밀번호를 암호화해서 저장! ("1234" -> "$2a$10$sdf32...")
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