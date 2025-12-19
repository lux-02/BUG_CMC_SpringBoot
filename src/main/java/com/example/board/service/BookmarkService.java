package com.example.board.service;

import com.example.board.domain.Bookmark;
import com.example.board.domain.Post;
import com.example.board.domain.User;
import com.example.board.repository.BookmarkRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public void toggleBookmark(Long userId, Long postId) {
        // 1. 유저와 게시글이 진짜 있는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 2. 이미 북마크가 되어 있는지 확인
        Optional<Bookmark> bookmarkOptional = bookmarkRepository.findByUserIdAndPostId(userId, postId);

        if (bookmarkOptional.isPresent()) {
            // 3-A. 이미 있으면 -> 삭제 (취소)
            bookmarkRepository.delete(bookmarkOptional.get());
        } else {
            // 3-B. 없으면 -> 추가 (저장)
            Bookmark bookmark = new Bookmark(user, post);
            bookmarkRepository.save(bookmark);
        }
    }
}