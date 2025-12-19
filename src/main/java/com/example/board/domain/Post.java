package com.example.board.domain;

import com.example.board.exception.UnauthorizedException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Post(String title, String content, User user, Category category, LocalDateTime createdAt) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.category = category;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // 팩토리 메서드
    public static Post create(String title, String content, User author, Category category) {
        return Post.builder()
                .title(title)
                .content(content)
                .user(author)
                .category(category)
                .build();
    }

    // 비즈니스 로직: 권한 검증을 도메인에서 처리
    public boolean isOwnedBy(User user) {
        return this.user.getId().equals(user.getId());
    }

    public void validateOwnership(User user) {
        if (!isOwnedBy(user)) {
            throw new UnauthorizedException("작성자만 수정/삭제할 수 있습니다.");
        }
    }

    // 비즈니스 로직: 수정
    public void update(String title, String content, User editor) {
        validateOwnership(editor);
        this.title = title;
        this.content = content;
    }

}