package com.example.board.service;

import com.example.board.domain.Comment;
import com.example.board.domain.Post;
import com.example.board.domain.User;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("대댓글 작성 성공: 부모 댓글이 존재하면 자식 댓글로 저장된다")
    void writeReply_Success() {
        // 1. Given (준비)
        Long userId = 1L;
        Long postId = 1L;
        Long parentId = 10L; // 부모 댓글 ID
        String content = "이것은 대댓글입니다.";

        User user = new User();
        Post post = new Post();

        // 부모 댓글 객체 (가짜)
        Comment parentComment = new Comment();
        // (만약 Comment에도 public 생성자가 없다면 User때처럼 추가해줘야 합니다!)

        // - 유저, 게시글, 부모 댓글 모두 DB에 있다고 가정
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findById(parentId)).willReturn(Optional.of(parentComment));
        given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> invocation.getArgument(0));

        // 2. When (실행)
        // (참고: writeComment 메서드 파라미터 순서는 본인 코드에 맞게 수정하세요!)
        commentService.writeComment(userId, postId, parentId, content);

        // 3. Then (검증)
        // - 저장된 댓글을 납치(Capture)해서 부모가 잘 세팅되었는지 확인
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());

        Comment savedComment = commentCaptor.getValue();

        // - 검증 1: 내용이 맞는가?
        assertEquals(content, savedComment.getContent());

        // - 검증 2: 부모 댓글이 세팅되었는가? (가장 중요 ★)
        assertNotNull(savedComment.getParent());
        assertEquals(parentComment, savedComment.getParent());
    }

    @Test
    @DisplayName("대댓글 작성 실패: 부모 댓글이 없으면 에러가 발생한다")
    void writeReply_Fail_NoParent() {
        // 1. Given
        Long userId = 1L;
        Long postId = 1L;
        Long invalidParentId = 999L; // 없는 댓글 ID

        given(userRepository.findById(userId)).willReturn(Optional.of(new User()));
        given(postRepository.findById(postId)).willReturn(Optional.of(new Post()));

        // - 부모 댓글을 찾았는데 없다(Empty)고 설정
        given(commentRepository.findById(invalidParentId)).willReturn(Optional.empty());

        // 2. When & Then (실행 및 예외 검증)
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.writeComment(userId, postId, invalidParentId, "내용");
        });
    }
}