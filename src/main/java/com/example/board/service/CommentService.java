package com.example.board.service;

import com.example.board.domain.Comment;
import com.example.board.domain.Post;
import com.example.board.domain.User;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    // [댓글 작성] parentId가 null이면 일반 댓글, 값이 있으면 대댓글
    public Long writeComment(Long userId, Long postId, Long parentId, String content) {
        User user = userRepository.findById(userId).orElseThrow();
        Post post = postRepository.findById(postId).orElseThrow();

        Comment parent = null;
        if (parentId != null) {
            parent = commentRepository.findById(parentId).orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
        }
        Comment comment = new Comment(content, user, post, parent);
        Comment savedComment = commentRepository.save(comment);
        return savedComment.getId();
    }
}