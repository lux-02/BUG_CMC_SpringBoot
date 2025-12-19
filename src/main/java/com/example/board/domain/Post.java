package com.example.board.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;

    // [중요] 작성자와의 관계 (N : 1)
    @ManyToOne
    @JoinColumn(name = "user_id") // DB에 생성될 컬럼 이름
    private User user;

    // [중요] 카테고리와의 관계 (N : 1)
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private LocalDateTime createdAt;

    public Post() {}

    public Post(String title, String content, User user, Category category) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }

    // Getter
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }

    // [추가] 게시글 수정 메서드 (비즈니스 로직)
    /*
    왜 Setter(setTitle)를 안 쓰고 update를 만드나요?
    무작정 Setter를 열어두면 누가 어디서 데이터를 바꿨는지 찾기 힘듭니다.
    이렇게 update라는 이름으로 명확하게 "수정할 때만 써!"라고 만들어두는 게
    유지보수에 훨씬 좋습니다.
    */

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public Category getCategory() {
        return category;
    }

}