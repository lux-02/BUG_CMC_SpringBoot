package com.example.board.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. 비밀번호 암호화 기계 등록 (BCrypt 방식)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 보안 필터 체인 (문지기 설정)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보안 설정 비활성화 (실습 편의상)
                .csrf(csrf -> csrf.disable())

                // 요청 주소별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 누구나 접근 가능 (회원가입, 로그인, H2 콘솔)
                        .requestMatchers("/users/signup", "/login", "/h2-console/**").permitAll()

                        // ADMIN만 접근 가능 (스웨거 문서)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN")

                        // 나머지는 로그인한 사람만 접근 가능
                        .anyRequest().authenticated()
                )

                // 폼 로그인 방식 사용 (스프링 시큐리티가 기본 로그인 페이지 제공)
                .formLogin(form -> form
                        .defaultSuccessUrl("/posts", true) // 로그인 성공 시 게시글 목록으로 이동
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/") // 로그아웃 시 메인으로 이동
                )

                // H2 콘솔 사용을 위한 설정 (Iframe 허용)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}