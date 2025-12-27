package com.example.board.controller;

import com.example.board.domain.Post;
import com.example.board.domain.User;
import com.example.board.dto.PostRequestDto; // [변경] DTO 교체
import com.example.board.dto.PostUpdateRequest;
import com.example.board.repository.UserRepository;
import com.example.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor // 생성자 주입 자동화 (lombok)
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

    // [글 쓰기] POST http://localhost:8080/api/posts
    @PostMapping("/posts")
    public String writePost(
            @RequestBody PostRequestDto request, // ★ [수정] PostRequestDto로 변경
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 1. 로그인 여부 확인
        // (테스트 환경에서 @AutoConfigureMockMvc(addFilters = false)를 쓰면 userDetails가 null일 수 있음)
        if (userDetails == null) {
            // ★ 테스트 편의상: 로그인이 안 된 상태(테스트)라면 DTO에 있는 ID를 그대로 씁니다.
            // (실제 운영 때는 이 else 분기를 없애고 무조건 에러를 뱉게 해야 안전합니다)
            if (request.getUserId() != null) {
                Long postId = postService.writePost(
                        request.getUserId(),
                        request.getCategoryId(),
                        request.getTitle(),
                        request.getContent()
                );
                return "게시글 작성 성공! (테스트 모드) 글 ID: " + postId;
            }
            return "로그인이 필요합니다.";
        }

        // 2. 로그인된 경우: 진짜 유저 ID 찾기 (보안상 이게 맞음)
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원이 아닙니다."));

        // 3. 서비스 실행 (DTO 내용 + 진짜 유저 ID)
        Long postId = postService.writePost(
                user.getId(), // ★ DTO의 userId는 무시하고, 실제 로그인한 ID 사용
                request.getCategoryId(),
                request.getTitle(),
                request.getContent()
        );

        return "게시글 작성 성공! 글 ID: " + postId;
    }

    // [글 조회]
    @GetMapping("/posts")
    public List<Post> getPostList() {
        return postService.findAllPosts();
    }

    // [글 수정]
    @PutMapping("/posts/{postId}")
    public String updatePost(
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) return "로그인이 필요합니다.";

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원이 아닙니다."));

        postService.updatePost(postId, user, request.getTitle(), request.getContent());
        return "게시글 수정 완료!";
    }

    // [글 삭제]
    @DeleteMapping("/posts/{postId}")
    public String deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) return "로그인이 필요합니다.";

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원이 아닙니다."));

        postService.deletePost(postId, user);
        return "게시글 삭제 완료!";
    }
}