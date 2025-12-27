package com.example.board.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    // [핵심] 대댓글 기능: 내 부모 댓글이 누구인지 가리킴
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    private LocalDateTime createdAt;

    public Comment() {

    }

    // 일반 댓글용 생성자
    public Comment(String content, User user, Post post) {
        this(content, user, post, null);
    }

    // 대댓글용 생성자
    public Comment(String content, User user, Post post, Comment parent) {
        this.content = content;
        this.user = user;
        this.post = post;
        this.parent = parent;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getContent() { return content; }
    public User getUser() { return user; }
    public Post getPost() { return post; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public Comment getParent() {
        return this.parent;
    }

}

