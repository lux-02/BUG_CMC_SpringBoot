package com.example.board.controller;

import com.example.board.dto.PostRequestDto;
import com.example.board.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // @AuthenticationPrincipal 어노테이션이 null을 반환하도록 커스텀 리졸버 설정
        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(org.springframework.core.MethodParameter parameter, org.springframework.web.method.support.ModelAndViewContainer mavContainer, org.springframework.web.context.request.NativeWebRequest webRequest, org.springframework.web.bind.support.WebDataBinderFactory binderFactory) throws Exception {
                return null; // 인증되지 않은 사용자를 시뮬레이션
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(postController)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .build();
    }

    @Test
    @DisplayName("글 작성 API 호출 성공 테스트")
    void writePost_Api_Success() throws Exception {
        // 1. Given
        // Setter 대신 생성자를 사용하여 객체 생성
        PostRequestDto requestDto = new PostRequestDto(1L, 1L, "테스트 제목", "테스트 내용");

        String jsonBody = objectMapper.writeValueAsString(requestDto);

        given(postService.writePost(eq(1L), eq(1L), eq("테스트 제목"), eq("테스트 내용"))).willReturn(1L);

        // 2. When
        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                // 3. Then
                .andExpect(status().isOk())
                .andDo(print());

        verify(postService).writePost(
                eq(1L), eq(1L), eq("테스트 제목"), eq("테스트 내용")
        );
    }
}
