package com.example.board.service;

import com.example.board.domain.Role;
import com.example.board.domain.User;
import com.example.board.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    // ★ 변경점 1: Mock(가짜) 대신 Spy(진짜 객체 스파이) 사용
    // 실제 BCrypt 로직이 돌아갑니다.
    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공: 실제 BCrypt로 암호화되어 저장된다")
    void join_Success_RealBCrypt() {
        // 1. Given
        String email = "real_bcrypt@example.com";
        String rawPassword = "1234";
        Role role = Role.USER;

        // (주의) @Spy를 쓸 때는 stubbing(given)을 하지 않아도 됩니다.
        // 실제 객체의 메서드가 호출되기 때문입니다.

        // DB 저장은 성공한다고 가정
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // 2. When
        userService.join(email, rawPassword, role);

        // 3. Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        // ★ 검증 포인트
        // 1. 원래 비밀번호("1234")와 저장된 비밀번호가 달라야 한다 (암호화 됨)
        assertNotEquals(rawPassword, savedUser.getPassword());

        // 2. 저장된 비밀번호가 "1234"를 암호화한 것이 맞는지 확인한다.
        // BCrypt는 매번 결과가 달라지므로 문자열 비교(equals)가 불가능하고, matches()를 써야 합니다.
        assertTrue(passwordEncoder.matches(rawPassword, savedUser.getPassword()));

        // 3. 암호화된 비밀번호가 BCrypt 형식이면 보통 60자입니다.
        assertTrue(savedUser.getPassword().length() >= 60);
    }
}