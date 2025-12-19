# 🔍 코드 리뷰 상세 분석

## 📋 목차
1. [이펙티브 자바 (Effective Java)](#1-이펙티브-자바)
2. [디자인 패턴 (Design Patterns)](#2-디자인-패턴)
3. [SOLID 객체지향 원칙](#3-solid-객체지향-원칙)
4. [아키텍처 (Architecture)](#4-아키텍처)
5. [개선 요약](#5-개선-요약)

---

## 1. 이펙티브 자바

### 🔴 문제점 1: 불변성(Immutability) 부족

**기존 코드 (User.java)**
```java
@Entity
public class User {
    private Long id;
    private String email;
    private String password;
    private Role role;
    private LocalDateTime createdAt;
    
    // Getter만 있고 Setter 없음 (좋음!)
    // 하지만 생성 후 객체 상태 변경 방법이 없음 (좋지 않음!)
}
```

**문제점:**
- ❌ **Item 17: Minimize mutability** 위반
- 생성 후 필드를 변경할 수 없어 유연성 부족
- `createdAt`을 생성자에서 자동 설정하는 것은 좋지만, 테스트하기 어려움

**✅ 개선 방안:**
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Builder
    private User(String email, String password, Role role, LocalDateTime createdAt) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }
    
    // 비즈니스 메서드: 명확한 의도 표현
    public void changePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
    }
}
```

**개선 효과:**
- ✅ **@Builder 패턴** 사용으로 가독성 향상
- ✅ `AccessLevel.PROTECTED`로 JPA 요구사항 충족하면서 안전성 확보
- ✅ 테스트 시 `createdAt` 주입 가능
- ✅ 명확한 비즈니스 메서드로 의도 표현

---

### 🔴 문제점 2: 예외 처리의 일관성 부족

**기존 코드 (PostService.java)**
```java
public Long writePost(Long userId, Long categoryId, String title, String content) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
    // ...
}
```

**문제점:**
- ❌ **Item 72: Favor the use of standard exceptions** 일부 위반
- `IllegalArgumentException`은 '잘못된 인자'를 의미하지만, 여기서는 '존재하지 않는 리소스'
- 모든 예외가 `IllegalArgumentException`으로 동일하여 구분 불가
- 예외 메시지만으로 에러 타입 판단

**✅ 개선 방안:**
```java
// 커스텀 예외 정의
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("User not found: " + userId);
    }
}

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long categoryId) {
        super("Category not found: " + categoryId);
    }
}

// 서비스 코드
public Long writePost(Long userId, Long categoryId, String title, String content) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    // ...
}
```

**개선 효과:**
- ✅ 예외 타입만으로 에러 원인 파악 가능
- ✅ GlobalExceptionHandler에서 세밀한 에러 응답 가능
- ✅ 의미론적으로 더 명확함

---

### 🔴 문제점 3: DTO에 Lombok @Data 사용

**기존 코드 (PostRequestDto.java)**
```java
@Data  // ❌ 위험!
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDto {
    private Long userId;
    private Long categoryId;
    private String title;
    private String content;
}
```

**문제점:**
- ❌ **Item 16: Minimize the accessibility of classes and members** 위반
- `@Data`는 `@Getter`, `@Setter`, `@ToString`, `@EqualsAndHashCode`, `@RequiredArgsConstructor`를 모두 포함
- **Setter가 자동 생성**되어 불변성 깨짐
- `toString()`과 `hashCode()`가 의도치 않게 노출

**✅ 개선 방안:**
```java
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PostRequestDto {
    private Long userId;      // 실제로는 SecurityContext에서 가져올 예정
    private Long categoryId;
    private String title;
    private String content;
    
    // 팩토리 메서드 (명확한 의도)
    public static PostRequestDto of(Long categoryId, String title, String content) {
        return PostRequestDto.builder()
                .categoryId(categoryId)
                .title(title)
                .content(content)
                .build();
    }
}
```

**개선 효과:**
- ✅ 불변 객체로 변경
- ✅ Setter 제거로 예측 가능한 코드
- ✅ Builder 패턴으로 가독성 향상

---

### 🔴 문제점 4: Optional 사용 미흡

**기존 코드 (PostService.java)**
```java
public Post findOne(Long postId) {
    return postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
}
```

**문제점:**
- ❌ **Item 55: Return optionals judiciously** 부분 위반
- Service에서 항상 예외를 던지므로 Optional의 장점 활용 못함
- 호출자가 "존재하지 않을 수도 있다"는 시그니처를 알 수 없음

**✅ 개선 방안:**
```java
// 방법 1: Optional 반환 (호출자가 선택)
public Optional<Post> findPost(Long postId) {
    return postRepository.findById(postId);
}

// 방법 2: 존재 확인이 필수인 경우 명확한 네이밍
public Post getPostOrThrow(Long postId) {
    return postRepository.findById(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));
}
```

**개선 효과:**
- ✅ 메서드 시그니처만으로 의도 파악 가능
- ✅ 호출자가 존재하지 않을 경우를 처리할 수 있음

---

## 2. 디자인 패턴

### 🔴 문제점 5: Builder 패턴 미적용

**기존 코드 (Post.java)**
```java
public Post(String title, String content, User user, Category category) {
    this.title = title;
    this.content = content;
    this.user = user;
    this.category = category;
    this.createdAt = LocalDateTime.now();
}

// 사용 예시
Post post = new Post(title, content, user, category);
// 파라미터 순서 헷갈림, 가독성 낮음
```

**문제점:**
- ❌ **Builder 패턴** 미적용
- 파라미터가 많을수록 순서 실수 위험
- 선택적 파라미터 처리 어려움
- 가독성 낮음

**✅ 개선 방안:**
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {
    // ... 필드 생략
    
    @Builder
    private Post(String title, String content, User author, Category category, LocalDateTime createdAt) {
        this.title = title;
        this.content = content;
        this.user = author;
        this.category = category;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }
    
    // 팩토리 메서드 (도메인 언어 사용)
    public static Post create(String title, String content, User author, Category category) {
        return Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .category(category)
                .build();
    }
}

// 사용 예시
Post post = Post.builder()
    .title("제목")
    .content("내용")
    .author(user)
    .category(category)
    .build();
```

**개선 효과:**
- ✅ 가독성 대폭 향상
- ✅ 파라미터 순서 실수 방지
- ✅ 선택적 필드 지원 용이

---

### 🔴 문제점 6: Strategy 패턴 미적용 (권한 검증)

**기존 코드 (PostService.java)**
```java
@Transactional
public void updatePost(Long postId, Long currentUserId, String newTitle, String newContent) {
    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

    // ❌ 권한 검증 로직이 Service에 하드코딩
    if (!post.getUser().getId().equals(currentUserId)) {
        throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
    }

    post.update(newTitle, newContent);
}

@Transactional
public void deletePost(Long postId, Long currentUserId) {
    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

    // ❌ 동일한 권한 검증 로직 중복
    if (!post.getUser().getId().equals(currentUserId)) {
        throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
    }

    postRepository.delete(post);
}
```

**문제점:**
- ❌ **Strategy 패턴** 미적용
- 권한 검증 로직이 여러 곳에 중복
- 권한 정책 변경 시 모든 곳 수정 필요
- ADMIN은 모든 글 삭제 가능 같은 정책 추가 어려움

**✅ 개선 방안:**
```java
// 권한 검증 Strategy 인터페이스
public interface AuthorizationStrategy {
    void authorize(User currentUser, Post post);
}

// 작성자 권한 검증
public class OwnerAuthorizationStrategy implements AuthorizationStrategy {
    @Override
    public void authorize(User currentUser, Post post) {
        if (!post.isOwnedBy(currentUser)) {
            throw new UnauthorizedException("작성자만 접근할 수 있습니다.");
        }
    }
}

// ADMIN 또는 작성자 권한 검증
public class AdminOrOwnerAuthorizationStrategy implements AuthorizationStrategy {
    @Override
    public void authorize(User currentUser, Post post) {
        if (currentUser.isAdmin()) {
            return; // ADMIN은 모든 권한
        }
        if (!post.isOwnedBy(currentUser)) {
            throw new UnauthorizedException("권한이 없습니다.");
        }
    }
}

// Post 도메인에 비즈니스 메서드 추가
public class Post {
    public boolean isOwnedBy(User user) {
        return this.user.getId().equals(user.getId());
    }
}

// Service 코드
@Transactional
public void updatePost(Long postId, User currentUser, String newTitle, String newContent) {
    Post post = getPostOrThrow(postId);
    
    // Strategy 주입 (DI)
    authorizationStrategy.authorize(currentUser, post);
    
    post.update(newTitle, newContent);
}
```

**개선 효과:**
- ✅ 권한 검증 로직 재사용
- ✅ 정책 변경 시 Strategy만 수정
- ✅ 테스트 용이성 향상 (Mock Strategy)

---

### 🔴 문제점 7: Factory 패턴 미적용

**기존 코드 (UserService.java)**
```java
@Transactional
public Long join(String email, String password, Role role) {
    String encodedPassword = passwordEncoder.encode(password);
    User user = new User(email, encodedPassword, role);
    userRepository.save(user);
    return user.getId();
}
```

**문제점:**
- ❌ **Factory 패턴** 미적용
- User 생성 로직이 Service에 존재
- 비밀번호 암호화 같은 생성 규칙이 분산됨
- 동일한 생성 로직이 여러 곳에 중복될 가능성

**✅ 개선 방안:**
```java
// User Factory
@Component
public class UserFactory {
    private final PasswordEncoder passwordEncoder;
    
    public UserFactory(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    public User createUser(String email, String rawPassword, Role role) {
        validateEmail(email);
        validatePassword(rawPassword);
        
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .role(role)
                .build();
    }
    
    private void validateEmail(String email) {
        if (!email.contains("@")) {
            throw new InvalidEmailException(email);
        }
    }
    
    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new WeakPasswordException();
        }
    }
}

// Service 코드
@Transactional
public Long join(String email, String password, Role role) {
    User user = userFactory.createUser(email, password, role);
    userRepository.save(user);
    return user.getId();
}
```

**개선 효과:**
- ✅ 생성 로직 중앙화
- ✅ 검증 로직 포함
- ✅ 테스트 시 Factory만 Mock 가능

---

## 3. SOLID 객체지향 원칙

### 🔴 문제점 8: SRP (Single Responsibility Principle) 위반

**기존 코드 (PostController.java)**
```java
@PostMapping("/posts")
public String writePost(
        @RequestBody PostRequestDto request,
        @AuthenticationPrincipal UserDetails userDetails
) {
    // ❌ Controller가 인증 처리 + 사용자 조회 + 비즈니스 로직 호출
    if (userDetails == null) {
        if (request.getUserId() != null) {
            Long postId = postService.writePost(/*...*/);
            return "게시글 작성 성공! (테스트 모드) 글 ID: " + postId;
        }
        return "로그인이 필요합니다.";
    }

    // ❌ Controller가 Repository 직접 조회
    User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("회원이 아닙니다."));

    Long postId = postService.writePost(/*...*/);
    return "게시글 작성 성공! 글 ID: " + postId;
}
```

**문제점:**
- ❌ **SRP 위반**: Controller가 너무 많은 책임
  - HTTP 요청/응답 처리
  - 인증 상태 확인
  - 사용자 조회
  - 비즈니스 로직 호출
- Controller가 Repository에 직접 의존 (계층 위반)

**✅ 개선 방안:**
```java
// 현재 사용자 조회 서비스
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    
    public User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException(userDetails.getUsername()));
    }
}

// Controller: 단순히 HTTP 처리만
@PostMapping("/posts")
public ResponseEntity<PostResponse> writePost(
        @RequestBody @Valid PostCreateRequest request,
        @AuthenticationPrincipal UserDetails userDetails
) {
    User currentUser = authenticationService.getCurrentUser(userDetails);
    
    Long postId = postService.writePost(
            currentUser.getId(),
            request.getCategoryId(),
            request.getTitle(),
            request.getContent()
    );
    
    return ResponseEntity.created(URI.create("/posts/" + postId))
            .body(PostResponse.of(postId));
}
```

**개선 효과:**
- ✅ Controller는 HTTP 처리만 담당
- ✅ 인증 로직은 AuthenticationService로 분리
- ✅ Repository 의존성 제거

---

### 🔴 문제점 9: OCP (Open-Closed Principle) 위반

**기존 코드 (SecurityConfig.java)**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/users/signup", "/login", "/h2-console/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        );
    // ...
}
```

**문제점:**
- ❌ **OCP 위반**: 새로운 권한 규칙 추가 시 이 클래스 수정 필요
- 권한 규칙이 하드코딩되어 있음
- 정책 변경 시 설정 클래스 수정

**✅ 개선 방안:**
```java
// 권한 규칙 정의
public interface SecurityRule {
    String[] getPatterns();
    boolean isPublic();
    String[] getRoles();
}

@Component
public class PublicEndpointsRule implements SecurityRule {
    @Override
    public String[] getPatterns() {
        return new String[]{"/users/signup", "/login", "/h2-console/**"};
    }
    
    @Override
    public boolean isPublic() {
        return true;
    }
    
    @Override
    public String[] getRoles() {
        return new String[]{};
    }
}

@Component
public class AdminOnlyEndpointsRule implements SecurityRule {
    @Override
    public String[] getPatterns() {
        return new String[]{"/swagger-ui/**", "/v3/api-docs/**", "/admin/**"};
    }
    
    @Override
    public boolean isPublic() {
        return false;
    }
    
    @Override
    public String[] getRoles() {
        return new String[]{"ADMIN"};
    }
}

// SecurityConfig
@Bean
public SecurityFilterChain filterChain(HttpSecurity http, List<SecurityRule> rules) throws Exception {
    http.authorizeHttpRequests(auth -> {
        rules.forEach(rule -> {
            if (rule.isPublic()) {
                auth.requestMatchers(rule.getPatterns()).permitAll();
            } else {
                auth.requestMatchers(rule.getPatterns()).hasAnyRole(rule.getRoles());
            }
        });
        auth.anyRequest().authenticated();
    });
    // ...
}
```

**개선 효과:**
- ✅ 새로운 권한 규칙 추가 시 SecurityRule 구현체만 추가
- ✅ SecurityConfig 수정 불필요
- ✅ 테스트 용이

---

### 🔴 문제점 10: DIP (Dependency Inversion Principle) 미흡

**기존 코드**
```java
// Repository 인터페이스 (JpaRepository 상속)
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

// Service가 구체적인 JPA 구현에 의존
@Service
public class UserService {
    private final UserRepository userRepository; // JpaRepository에 의존
    // ...
}
```

**문제점:**
- ⚠️ **DIP 부분 위반**: JpaRepository는 Spring Data JPA 구현
- 다른 저장소 기술(MongoDB, Redis)로 변경 시 Service 영향
- 도메인 계층이 인프라에 의존

**✅ 개선 방안:**
```java
// 도메인 계층의 추상화
public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
}

// 인프라 계층의 구현
@Repository
public class JpaUserRepository implements UserRepository {
    private final SpringDataUserRepository springDataUserRepository;
    
    @Override
    public User save(User user) {
        return springDataUserRepository.save(user);
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id);
    }
    // ...
}

// Spring Data JPA 인터페이스 (인프라 계층에만 존재)
interface SpringDataUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

**개선 효과:**
- ✅ 도메인이 인프라에 의존하지 않음
- ✅ 저장소 기술 변경 용이
- ✅ 진정한 계층형 아키텍처

**주의:** 실무에서는 Spring Data JPA를 직접 사용하는 것이 일반적. 
과도한 추상화는 오히려 복잡도를 높일 수 있으므로 프로젝트 규모에 따라 판단 필요.

---

## 4. 아키텍처

### 🔴 문제점 11: 도메인 로직이 Service에 분산

**기존 코드**
```java
// PostService.java
@Transactional
public void updatePost(Long postId, Long currentUserId, String newTitle, String newContent) {
    Post post = postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

    // ❌ 권한 검증이 Service에 존재
    if (!post.getUser().getId().equals(currentUserId)) {
        throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
    }

    post.update(newTitle, newContent);
}
```

**문제점:**
- ❌ **빈혈 도메인 모델 (Anemic Domain Model)**: 도메인 객체가 데이터만 담고 로직 없음
- 비즈니스 로직이 Service 계층에 집중
- 도메인 지식이 분산되어 응집도 낮음

**✅ 개선 방안 - 풍부한 도메인 모델 (Rich Domain Model)**
```java
// Post.java (도메인)
@Entity
public class Post {
    // ... 필드 생략
    
    // 비즈니스 로직을 도메인에 배치
    public void validateOwnership(User user) {
        if (!this.user.getId().equals(user.getId())) {
            throw new UnauthorizedException("작성자만 수정할 수 있습니다.");
        }
    }
    
    public boolean isOwnedBy(User user) {
        return this.user.getId().equals(user.getId());
    }
    
    public void update(String title, String content, User editor) {
        validateOwnership(editor); // 검증 로직도 도메인에
        this.title = title;
        this.content = content;
    }
    
    public void delete(User deleter) {
        validateOwnership(deleter);
        // soft delete 구현 시
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}

// PostService.java (애플리케이션 서비스)
@Transactional
public void updatePost(Long postId, User currentUser, String newTitle, String newContent) {
    Post post = getPostOrThrow(postId);
    
    // Service는 도메인 객체 조율만
    post.update(newTitle, newContent, currentUser);
    // JPA가 변경 감지하여 자동 저장
}
```

**개선 효과:**
- ✅ 도메인 로직이 도메인 객체에 응집
- ✅ Service는 트랜잭션과 도메인 객체 조율만 담당
- ✅ 도메인 지식이 한 곳에 모여 유지보수 용이

---

### 🔴 문제점 12: 응답 DTO 부재

**기존 코드 (PostController.java)**
```java
@GetMapping("/posts")
public List<Post> getAllPosts() {
    return postService.findAllPosts(); // ❌ Entity 직접 노출
}
```

**문제점:**
- ❌ **Entity 직접 노출**: API 스펙 변경 시 Entity 수정 필요
- 순환 참조 위험 (Post -> User -> Post)
- 민감 정보 노출 위험 (password 등)
- API 버전 관리 어려움

**✅ 개선 방안**
```java
// 응답 DTO
@Getter
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private String categoryName;
    private LocalDateTime createdAt;
    
    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorName(post.getUser().getEmail())
                .categoryName(post.getCategory().getName())
                .createdAt(post.getCreatedAt())
                .build();
    }
}

// Controller
@GetMapping("/posts")
public ResponseEntity<List<PostResponse>> getAllPosts() {
    List<Post> posts = postService.findAllPosts();
    List<PostResponse> responses = posts.stream()
            .map(PostResponse::from)
            .toList();
    return ResponseEntity.ok(responses);
}
```

**개선 효과:**
- ✅ Entity와 API 스펙 분리
- ✅ 필요한 데이터만 노출
- ✅ API 버전 관리 용이

---

### 🔴 문제점 13: 트랜잭션 범위 과도

**기존 코드 (PostService.java)**
```java
@Service
@Transactional(readOnly = true) // ❌ 클래스 레벨 readOnly
public class PostService {
    
    @Transactional // ⚠️ readOnly 오버라이드
    public Long writePost(/*...*/) {
        // ...
    }
}
```

**문제점:**
- ⚠️ 클래스 레벨 `@Transactional`은 명시적이지 않음
- 새로운 메서드 추가 시 트랜잭션 설정 실수 가능
- readOnly 오버라이드 누락 가능

**✅ 개선 방안**
```java
@Service
public class PostService {
    
    @Transactional
    public Long writePost(/*...*/) {
        // 쓰기 작업
    }
    
    @Transactional(readOnly = true)
    public Post findPost(Long postId) {
        // 읽기 작업
    }
    
    @Transactional(readOnly = true)
    public List<Post> findAllPosts() {
        // 읽기 작업
    }
}
```

**개선 효과:**
- ✅ 각 메서드의 트랜잭션 의도가 명확
- ✅ readOnly 최적화를 명시적으로 적용
- ✅ 실수 방지

---

## 5. 개선 요약

### 📊 개선 전후 비교표

| 항목 | 기존 코드 | 개선 코드 | 효과 |
|-----|----------|----------|------|
| **불변성** | Setter 없지만 생성 제약 | @Builder + 명확한 메서드 | 안전성 ↑ |
| **예외 처리** | IllegalArgumentException 일괄 | 커스텀 예외 세분화 | 가독성 ↑ |
| **DTO** | @Data 사용 | @Getter + @Builder | 불변성 ↑ |
| **도메인 로직** | Service에 분산 | 도메인에 응집 | 응집도 ↑ |
| **권한 검증** | 중복 코드 | Strategy 패턴 | 재사용성 ↑ |
| **Factory** | Service에서 생성 | Factory 분리 | 단일책임 ↑ |
| **Controller** | 다중 책임 | HTTP 처리만 | 명확성 ↑ |
| **응답** | Entity 직접 노출 | DTO 변환 | 안정성 ↑ |

### 🎯 핵심 교훈

1. **도메인 중심 설계**: 비즈니스 로직은 도메인 객체에
2. **명확한 책임 분리**: 각 계층과 클래스는 하나의 책임만
3. **확장 가능한 구조**: 새로운 기능 추가 시 기존 코드 수정 최소화
4. **타입 안전성**: 커스텀 예외, 명확한 DTO로 컴파일 타임 검증
5. **테스트 용이성**: 의존성 주입과 인터페이스로 Mock 가능하게

