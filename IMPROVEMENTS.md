# 코드 리뷰 개선 요약

## 📝 개선 사항 목록

### 1. 이펙티브 자바 관련 개선 (4개)

#### ✅ User, Post 엔티티에 Builder 패턴 적용
- **파일**: `User.java`, `Post.java`
- **Before**: 생성자에 많은 파라미터, 테스트 시 createdAt 주입 불가
- **After**: `@Builder` 적용, 가독성 향상, 테스트 용이
- **효과**: 객체 생성 시 명확성 증가, 파라미터 순서 실수 방지

#### ✅ 커스텀 예외 클래스 도입
- **파일**: `exception/` 패키지
- **Before**: 모든 에러가 `IllegalArgumentException`
- **After**: `UserNotFoundException`, `PostNotFoundException`, `CategoryNotFoundException`, `UnauthorizedException`
- **효과**: 예외 타입만으로 원인 파악, GlobalExceptionHandler에서 세밀한 응답 가능

#### ✅ DTO에서 @Data 제거, @Getter + @Builder 사용
- **파일**: `PostCreateRequest.java`, `PostResponse.java`
- **Before**: `@Data` 사용으로 불필요한 Setter 노출
- **After**: `@Getter`만 사용, 불변 객체로 변경
- **효과**: 의도치 않은 객체 변경 방지, 안전성 증가

#### ✅ Optional 사용 개선
- **파일**: `PostService.java`
- **Before**: `findOne()` - 메서드명이 불명확
- **After**: `getPostOrThrow()` - 예외를 던진다는 의도 명확
- **효과**: 메서드 시그니처만으로 동작 이해 가능

---

### 2. 디자인 패턴 관련 개선 (3개)

#### ✅ Builder 패턴 적용
- **파일**: `User.java`, `Post.java`
- **Before**: 생성자 파라미터 순서 헷갈림
- **After**: 
```java
Post.builder()
    .title("제목")
    .content("내용")
    .user(author)
    .category(category)
    .build();
```
- **효과**: 가독성 대폭 향상, 선택적 필드 지원 용이

#### ✅ 풍부한 도메인 모델 (Rich Domain Model)
- **파일**: `Post.java`
- **Before**: 권한 검증 로직이 Service에 존재
- **After**: `Post.isOwnedBy()`, `Post.validateOwnership()` 메서드 추가
- **효과**: 도메인 로직이 도메인 객체에 응집, 재사용성 증가

#### ✅ DTO 패턴 - Response DTO 도입
- **파일**: `PostResponse.java`
- **Before**: Entity 직접 반환 (순환 참조 위험, 민감정보 노출)
- **After**: `PostResponse.from(post)` 정적 팩토리 메서드
- **효과**: Entity와 API 스펙 분리, 필요한 데이터만 노출

---

### 3. SOLID 원칙 관련 개선 (3개)

#### ✅ SRP - AuthenticationService 분리
- **파일**: `AuthenticationService.java`
- **Before**: Controller가 UserRepository 직접 조회
- **After**: 현재 사용자 조회 책임을 AuthenticationService에 위임
- **효과**: Controller는 HTTP 처리만, 계층 책임 명확화

#### ✅ 커스텀 예외로 세밀한 예외 처리
- **파일**: `GlobalExceptionHandler.java`
- **Before**: 모든 예외를 400 에러로 반환
- **After**: 
  - `UserNotFoundException` → 404
  - `UnauthorizedException` → 403
  - `IllegalArgumentException` → 400
- **효과**: HTTP 상태 코드와 예외 타입의 의미론적 일치

#### ✅ 트랜잭션 범위 명확화
- **파일**: `PostService.java`, `UserService.java`
- **Before**: 클래스 레벨 `@Transactional(readOnly = true)`
- **After**: 메서드별로 명시적 트랜잭션 설정
- **효과**: 각 메서드의 트랜잭션 의도 명확, 실수 방지

---

### 4. 아키텍처 관련 개선 (2개)

#### ✅ 도메인 주도 설계 (Domain-Driven Design)
- **파일**: `Post.java`
- **Before**: 빈혈 도메인 모델 (데이터만 담음)
- **After**: 
```java
public void update(String title, String content, User editor) {
    validateOwnership(editor);  // 도메인이 검증
    this.title = title;
    this.content = content;
}
```
- **효과**: 비즈니스 로직이 도메인에 응집, Service는 조율만 담당

#### ✅ 계층 간 의존성 정리
- **파일**: `PostController.java`
- **Before**: Controller → Repository 직접 의존
- **After**: Controller → Service → Repository 계층 준수
- **효과**: 계층형 아키텍처 원칙 준수, 유지보수성 향상

---

## 📊 정량적 개선 지표

| 항목 | Before | After | 개선율 |
|-----|--------|-------|--------|
| 커스텀 예외 타입 | 1개 | 5개 | +400% |
| DTO 불변성 | 부분 (Setter 있음) | 완전 | 100% |
| 도메인 메서드 | 1개 (update) | 4개 | +300% |
| 계층 책임 분리 | 불명확 | 명확 | ✅ |
| 코드 가독성 | 보통 | 높음 | ✅ |

---

## 🎯 학습 포인트

### Before (문제점)
1. **빈혈 도메인**: 도메인 객체가 데이터만 담고 로직은 Service에
2. **단일 예외**: 모든 에러가 `IllegalArgumentException`
3. **불명확한 책임**: Controller가 Repository 직접 조회
4. **가독성 낮음**: 생성자 파라미터 순서 헷갈림

### After (개선)
1. **풍부한 도메인**: 비즈니스 로직이 도메인 객체에 응집
2. **세밀한 예외**: 예외 타입과 HTTP 상태 코드 일치
3. **명확한 책임**: 각 계층이 고유한 책임만 수행
4. **높은 가독성**: Builder 패턴으로 의도 명확

---

## 💡 실무 적용 가이드

### 1. Builder 패턴은 언제 쓸까?
- ✅ 파라미터가 4개 이상일 때
- ✅ 선택적 파라미터가 있을 때
- ✅ 불변 객체를 만들 때

### 2. 커스텀 예외는 언제 만들까?
- ✅ 특정 비즈니스 오류를 구분해야 할 때
- ✅ 호출자가 예외를 catch해서 특별 처리할 때
- ✅ HTTP 상태 코드를 세밀하게 구분할 때

### 3. 도메인에 로직을 넣어야 할까, Service에 넣어야 할까?
- **도메인**: 단일 객체 관련 검증, 상태 변경
  - 예: `post.validateOwnership()`, `post.update()`
- **Service**: 여러 도메인 객체 조율, 트랜잭션 관리
  - 예: `userService.join()` (User 생성 + 저장)

### 4. DTO는 어디까지 만들어야 할까?
- ✅ 요청 DTO: API 입력 검증, @Valid 적용
- ✅ 응답 DTO: Entity 노출 방지, 필요한 데이터만
- ❌ 과도한 DTO: Service 계층 간 전달용은 불필요

---

## 🔖 참고 자료

1. **Effective Java 3rd Edition** - Joshua Bloch
   - Item 2: Builder 패턴
   - Item 17: 불변성 최소화
   - Item 72: 표준 예외 사용

2. **Domain-Driven Design** - Eric Evans
   - Layered Architecture
   - Rich Domain Model
   - Ubiquitous Language

3. **Clean Code** - Robert C. Martin
   - Single Responsibility Principle
   - Meaningful Names
   - Error Handling

---

**작성일**: 2024-12-19  
**브랜치**: review  
**개선 항목**: 13개  
**핵심 패턴**: Builder, Rich Domain Model, Custom Exception

