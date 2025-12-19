package com.example.board.service;

import com.example.board.domain.Role;
import com.example.board.domain.User;
import com.example.board.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder; // [중요] 암호화 도구 임포트 추가됨
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 1. 암호화 변수 추가

    // 2. 생성자 수정 (받아오기)
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 3. 회원가입 메서드 수정 (암호화 로직 적용)
    @Transactional
    public Long join(String email, String password, Role role) {
        // passwordEncoder.encode(password)로 비밀번호를 암호화해서 저장! ("1234" -> "$2a$10$sdf32...")
        String encodedPassword = passwordEncoder.encode(password);

        User user = new User(email, encodedPassword, role);
        userRepository.save(user);
        return user.getId();
    }

    // [전체 회원 조회] - 기존 그대로 유지
    public List<User> findUsers() {
        return userRepository.findAll();
    }
}