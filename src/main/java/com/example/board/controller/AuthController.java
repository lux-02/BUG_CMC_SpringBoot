package com.example.board.controller;

import com.example.board.domain.Role;
import com.example.board.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 인증 관련 웹 페이지 컨트롤러
 * 로그인, 회원가입 등
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 메인 페이지 (루트)
     * GET /
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/web/posts";
    }

    /**
     * 로그인 페이지
     * GET /login
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        if (logout != null) {
            model.addAttribute("message", "로그아웃되었습니다.");
        }
        return "auth/login"; // templates/auth/login.html
    }

    /**
     * 회원가입 페이지
     * GET /users/signup
     */
    @GetMapping("/users/signup")
    public String signupPage() {
        return "auth/signup"; // templates/auth/signup.html
    }

    /**
     * 회원가입 처리
     * POST /users/signup
     */
    @PostMapping("/users/signup")
    public String signup(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String passwordConfirm,
            @RequestParam(required = false, defaultValue = "USER") String role,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        // 비밀번호 확인
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("email", email);
            return "auth/signup";
        }

        try {
            // 역할 변환
            Role userRole = Role.valueOf(role.toUpperCase());

            // 회원가입 처리
            userService.join(email, password, userRole);

            redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            return "auth/signup";
        }
    }
}

