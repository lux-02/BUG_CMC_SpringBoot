package com.example.board.service;

import com.example.board.domain.Bookmark;
import com.example.board.domain.Post;
import com.example.board.domain.User;
import com.example.board.repository.BookmarkRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock BookmarkRepository bookmarkRepository;
    @Mock PostRepository postRepository;
    @Mock UserRepository userRepository;

    @InjectMocks BookmarkService bookmarkService;

    @Test
    @DisplayName("북마크 추가: 기존에 북마크가 없으면 새로 저장한다")
    void toggleBookmark_Add() {
        // Given
        Long userId = 1L;
        Long postId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.of(new User()));
        given(postRepository.findById(postId)).willReturn(Optional.of(new Post()));

        // ★ 핵심: DB를 뒤져봤는데 아직 북마크한 적이 없다(Empty)
        given(bookmarkRepository.findByUserIdAndPostId(userId, postId))
                .willReturn(Optional.empty());

        // When
        bookmarkService.toggleBookmark(userId, postId);

        // Then
        // save(저장)가 호출되었는지 확인
        verify(bookmarkRepository).save(any(Bookmark.class));
        // delete(삭제)는 호출되지 않아야 함
        verify(bookmarkRepository, times(0)).delete(any(Bookmark.class));
    }

    @Test
    @DisplayName("북마크 취소: 이미 북마크가 있으면 삭제한다")
    void toggleBookmark_Remove() {
        // Given
        Long userId = 1L;
        Long postId = 1L;
        Bookmark existingBookmark = new Bookmark();

        given(userRepository.findById(userId)).willReturn(Optional.of(new User()));
        given(postRepository.findById(postId)).willReturn(Optional.of(new Post()));

        // ★ 핵심: DB를 뒤져보니 이미 북마크가 있다(Present)
        given(bookmarkRepository.findByUserIdAndPostId(userId, postId))
                .willReturn(Optional.of(existingBookmark));

        // When
        bookmarkService.toggleBookmark(userId, postId);

        // Then
        // delete(삭제)가 호출되었는지 확인
        verify(bookmarkRepository).delete(existingBookmark);
        // save(저장)는 호출되지 않아야 함
        verify(bookmarkRepository, times(0)).save(any(Bookmark.class));
    }
}