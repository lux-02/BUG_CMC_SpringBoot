package com.example.board.service;

import com.example.board.domain.Category;
import com.example.board.domain.Post;
import com.example.board.domain.User;
import com.example.board.repository.CategoryRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    // [게시글 작성]
    @Transactional
    public Long writePost(Long userId, Long categoryId, String title, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        Post post = new Post(title, content, user, category);
        postRepository.save(post);

        return post.getId();
    }

    // [게시글 목록 조회]
    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }

    // [특정 게시글 조회]
    public Post findOne(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
    }

    // ================= [새로 추가된 기능] =================

    // [게시글 수정]
    @Transactional
    public void updatePost(Long postId, Long currentUserId, String newTitle, String newContent) {
        // 1. 글 찾기
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        // 2. 작성자 검증 (글쓴이 ID vs 현재 로그인한 사람 ID)
        if (!post.getUser().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        // 3. 내용 수정 (JPA가 변경된 것을 감지하고 알아서 DB에 UPDATE 날려줌)
        post.update(newTitle, newContent);
    }

    // [게시글 삭제]
    @Transactional
    public void deletePost(Long postId, Long currentUserId) {
        // 1. 글 찾기
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        // 2. 작성자 검증
        if (!post.getUser().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        // 3. 삭제
        postRepository.delete(post);
    }
}