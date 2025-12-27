package com.example.board.controller;

import com.example.board.domain.Bookmark;
import com.example.board.domain.Category;
import com.example.board.domain.Comment;
import com.example.board.domain.Post;
import com.example.board.domain.User;
import com.example.board.repository.BookmarkRepository;
import com.example.board.repository.CategoryRepository;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.UserRepository;
import com.example.board.service.BookmarkService;
import com.example.board.service.CategoryService;
import com.example.board.service.CommentService;
import com.example.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thymeleaf 웹 페이지 컨트롤러
 * SSR(Server-Side Rendering) 방식으로 HTML을 생성하여 반환
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/web")
public class WebController {

    private final PostService postService;
    private final CategoryService categoryService;
    private final CommentService commentService;
    private final BookmarkService bookmarkService;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookmarkRepository bookmarkRepository;

    /**
     * 게시글 목록 페이지
     * GET /web/posts
     */
    @GetMapping("/posts")
    public String postList(
            @RequestParam(required = false) Long categoryId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        // 현재 로그인한 사용자 정보
        User currentUser = null;
        if (userDetails != null) {
            currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElse(null);
        }

        // 게시글 목록 조회 (카테고리 필터링)
        List<Post> posts;
        if (categoryId != null) {
            posts = postRepository.findByCategoryId(categoryId);
        } else {
            posts = postService.findAllPosts();
        }

        // 카테고리 목록 조회
        List<Category> categories = categoryRepository.findAll();

        model.addAttribute("posts", posts);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("currentUser", currentUser);

        return "posts/list"; // templates/posts/list.html
    }

    /**
     * 게시글 상세 페이지
     * GET /web/posts/{postId}
     */
    @GetMapping("/posts/{postId}")
    public String postDetail(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 현재 로그인한 사용자 정보
        User currentUser = null;
        boolean isBookmarked = false;
        if (userDetails != null) {
            currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElse(null);

            // 북마크 여부 확인
            if (currentUser != null) {
                isBookmarked = bookmarkRepository.findByUserIdAndPostId(currentUser.getId(), postId).isPresent();
            }
        }

        // 댓글 목록 조회 (생성 시간 오름차순)
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        model.addAttribute("post", post);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("comments", comments);
        model.addAttribute("isBookmarked", isBookmarked);

        return "posts/detail"; // templates/posts/detail.html
    }

    /**
     * 게시글 작성 페이지
     * GET /web/posts/new
     */
    @GetMapping("/posts/new")
    public String postForm(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "posts/form"; // templates/posts/form.html
    }

    /**
     * 게시글 작성 처리
     * POST /web/posts
     */
    @PostMapping("/posts")
    public String createPost(
            @RequestParam Long categoryId,
            @RequestParam String title,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원이 아닙니다."));

        Long postId = postService.writePost(user.getId(), categoryId, title, content);

        redirectAttributes.addFlashAttribute("message", "게시글이 작성되었습니다.");
        return "redirect:/web/posts/" + postId;
    }

    /**
     * 게시글 수정 페이지
     * GET /web/posts/{postId}/edit
     */
    @GetMapping("/posts/{postId}/edit")
    public String editPostForm(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원이 아닙니다."));

        // 작성자 본인만 수정 가능
        if (!post.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("post", post);
        model.addAttribute("categories", categories);

        return "posts/edit"; // templates/posts/edit.html
    }

    /**
     * 게시글 수정 처리
     * POST /web/posts/{postId}/edit
     */
    @PostMapping("/posts/{postId}/edit")
    public String updatePost(
            @PathVariable Long postId,
            @RequestParam String title,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원이 아닙니다."));

        postService.updatePost(postId, user.getId(), title, content);

        redirectAttributes.addFlashAttribute("message", "게시글이 수정되었습니다.");
        return "redirect:/web/posts/" + postId;
    }

    /**
     * 게시글 삭제 처리
     * POST /web/posts/{postId}/delete
     */
    @PostMapping("/posts/{postId}/delete")
    public String deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원이 아닙니다."));

        postService.deletePost(postId, user.getId());

        redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
        return "redirect:/web/posts";
    }

    /**
     * 카테고리 관리 페이지 (ADMIN 전용)
     * GET /web/categories
     */
    @GetMapping("/categories")
    public String categoryList(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "categories/list"; // templates/categories/list.html
    }

    /**
     * 카테고리 생성 처리 (ADMIN 전용)
     * POST /web/categories
     */
    @PostMapping("/categories")
    public String createCategory(
            @RequestParam String name,
            RedirectAttributes redirectAttributes
    ) {
        categoryService.createCategory(name);
        redirectAttributes.addFlashAttribute("message", "카테고리가 생성되었습니다.");
        return "redirect:/web/categories";
    }

    /**
     * 댓글 작성 처리
     * POST /web/posts/{postId}/comments
     */
    @PostMapping("/posts/{postId}/comments")
    public String createComment(
            @PathVariable Long postId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원이 아닙니다."));

        commentService.writeComment(user.getId(), postId, parentId, content);

        redirectAttributes.addFlashAttribute("message",
                parentId == null ? "댓글이 작성되었습니다." : "대댓글이 작성되었습니다.");
        return "redirect:/web/posts/" + postId;
    }

    /**
     * 댓글 삭제 처리
     * POST /web/comments/{commentId}/delete
     */
    @PostMapping("/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원이 아닙니다."));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자 본인만 삭제 가능
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        Long postId = comment.getPost().getId();
        commentRepository.delete(comment);

        redirectAttributes.addFlashAttribute("message", "댓글이 삭제되었습니다.");
        return "redirect:/web/posts/" + postId;
    }

    /**
     * 북마크 토글 (추가/삭제)
     * POST /web/posts/{postId}/bookmark
     */
    @PostMapping("/posts/{postId}/bookmark")
    public String toggleBookmark(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원이 아닙니다."));

        // 북마크 토글 (있으면 삭제, 없으면 추가)
        bookmarkService.toggleBookmark(user.getId(), postId);

        // 현재 북마크 상태 확인
        boolean isBookmarked = bookmarkRepository.findByUserIdAndPostId(user.getId(), postId).isPresent();

        redirectAttributes.addFlashAttribute("message",
                isBookmarked ? "북마크에 추가되었습니다." : "북마크가 해제되었습니다.");
        return "redirect:/web/posts/" + postId;
    }

    /**
     * 내 북마크 목록 페이지
     * GET /web/bookmarks
     */
    @GetMapping("/bookmarks")
    public String myBookmarks(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원이 아닙니다."));

        // 내 북마크 목록 조회
        List<Bookmark> bookmarks = bookmarkRepository.findByUserIdOrderByIdDesc(user.getId());

        // 북마크된 게시글 목록 추출
        List<Post> bookmarkedPosts = bookmarks.stream()
                .map(Bookmark::getPost)
                .collect(Collectors.toList());

        model.addAttribute("posts", bookmarkedPosts);
        model.addAttribute("currentUser", user);

        return "bookmarks/list"; // templates/bookmarks/list.html
    }
}

