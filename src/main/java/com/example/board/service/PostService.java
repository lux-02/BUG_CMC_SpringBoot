package com.example.board.service;

import com.example.board.domain.Category;
import com.example.board.domain.Post;
import com.example.board.domain.User;
import com.example.board.exception.CategoryNotFoundException;
import com.example.board.exception.PostNotFoundException;
import com.example.board.exception.UserNotFoundException;
import com.example.board.repository.CategoryRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 게시글 서비스
 * 개선 사항:
 * 1. 커스텀 예외 사용 (의미론적으로 명확)
 * 2. 도메인 로직 활용 (Post.update에서 권한 검증)
 * 3. 명확한 트랜잭션 범위 (메서드별)
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Long writePost(Long userId, Long categoryId, String title, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        // Builder 패턴 사용
        Post post = Post.builder()
                .title(title)
                .content(content)
                .user(user)
                .category(category)
                .build();

        postRepository.save(post);
        return post.getId();
    }

    @Transactional(readOnly = true)
    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Post getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    @Transactional
    public void updatePost(Long postId, User currentUser, String newTitle, String newContent) {
        Post post = getPostOrThrow(postId);

        // 도메인 객체가 권한 검증과 수정 담당
        post.update(newTitle, newContent, currentUser);
        // JPA 변경 감지로 자동 저장
    }

    @Transactional
    public void deletePost(Long postId, User currentUser) {
        Post post = getPostOrThrow(postId);

        // 권한 검증
        post.validateOwnership(currentUser);

        postRepository.delete(post);
    }
}