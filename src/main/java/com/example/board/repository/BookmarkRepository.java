package com.example.board.repository;

import com.example.board.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    // 유저 ID와 게시글 ID로 북마크 여부 확인
    Optional<Bookmark> findByUserIdAndPostId(Long userId, Long postId);
}