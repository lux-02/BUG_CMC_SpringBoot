package com.example.board.service;
import com.example.board.service.PostService;
import com.example.board.domain.Role;
import com.example.board.domain.User;
import com.example.board.domain.Category;
import com.example.board.domain.Post;
import com.example.board.repository.PostRepository;
import com.example.board.repository.UserRepository;
import com.example.board.repository.CategoryRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class) // Mockito 사용 선언
class PostServiceTest {

    @Mock // 가짜(Mock) 리포지토리 생성
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks // 가짜 리포지토리들을 주입받을 진짜 서비스
    private PostService postService;

    @Test
    @DisplayName("글 작성 성공 테스트")
    void writePost_Success() {
        // given (준비)
        Long userId = 1L;
        Long categoryId = 1L;
        // new User() 대신 데이터를 채워서 생성
        User user = new User("test@email.com", "1234", Role.USER);
        Category category = new Category(); // 테스트용 카테고리

        // "DB에서 찾으면 이 객체를 반환해라"라고 가짜 행동 정의
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // save 메서드는 아무것도 반환하지 않거나 엔티티를 반환하므로 설정 (여기선 호출 여부만 중요)
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            return savedPost; // 저장된 척 반환
        });

        // when (실행)
        postService.writePost(userId, categoryId, "테스트 제목", "테스트 내용");

        // then (검증)
        // postRepository.save()가 딱 1번 실행되었는지 확인
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("글 수정 실패 - 작성자가 아님")
    void updatePost_Fail_NotOwner() {
        // given
        Long postId = 1L;
        Long ownerId = 1L;
        Long hackerId = 2L; // 수정하려는 나쁜 사람

        // 원래 작성자 세팅 (Reflection 등을 쓰지 않고 간단히 Mock킹을 위해 로직 흐름만 테스트)
        // 실제로는 User 객체에 setId가 없으므로, Mock 객체를 활용하거나 생성자를 잘 써야 함.
        // 여기서는 흐름만 보여드리기 위해 로직 검증에 집중합니다.

        User owner = new User("owner@test.com", "pw", null);
        // User 엔티티에 setId가 없다면, 리플렉션으로 넣거나 MockUser를 써야 함.
        // 테스트 편의상 User의 getId() 호출 시 1L을 반환하도록 설정하는 게 좋음 (User도 Mocking 가능)

        // ★ User를 Mocking해서 ID 반환 조작
        User mockOwner = org.mockito.Mockito.mock(User.class);
        given(mockOwner.getId()).willReturn(ownerId);

        Post post = new Post("제목", "내용", mockOwner, new Category());

        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when & then (실행 및 예외 검증)
        // hackerId(2)로 수정을 시도하면 예외가 터져야 함
        assertThrows(IllegalArgumentException.class, () -> {
            postService.updatePost(postId, hackerId, "수정 제목", "수정 내용");
        });
    }

    @Test
    @DisplayName("게시글 삭제 성공: 작성자가 요청하면 글이 삭제된다")
    void deletePost_Success() {
        // 1. Given (주어진 상황)
        // - 게시글이 하나 있고, 그 글의 주인(userId:1)이 존재한다.
        Long postId = 1L;
        Long userId = 1L;

        User owner = new User(); // 아까 public으로 열어둔 생성자 활용
        // (실제로는 리플렉션이나 Mock을 통해 ID 1L을 주입해야 함. 여기선 Mockito 활용)
        User mockOwner = org.mockito.Mockito.mock(User.class);
        given(mockOwner.getId()).willReturn(userId);

        Post post = new Post("제목", "내용", mockOwner, new Category());

        // - DB에서 글을 찾으면 이 post를 반환한다고 가정한다.
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // 2. When (실행)
        // - 작성자(userId:1)가 글 삭제를 요청한다.
        postService.deletePost(postId, userId);

        // 3. Then (검증)
        // - 리포지토리의 delete() 메서드가 1번 호출되었는지 확인한다.
        verify(postRepository).delete(post);
    }
}