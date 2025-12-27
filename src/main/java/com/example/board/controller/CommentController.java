package com.example.board.controller;

import com.example.board.dto.CommentCreateRequest;
import com.example.board.service.CommentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // [댓글 쓰기] POST http://localhost:8080/api/comments
    @PostMapping(value = "/comments", produces = "text/plain;charset=UTF-8")
    public String writeComment(@RequestBody CommentCreateRequest request) {
        Long commentId = commentService.writeComment(
                request.getUserId(),
                request.getPostId(),
                request.getParentId(),
                request.getContent()
        );
        return "댓글 작성 성공! 댓글 ID: " + commentId;
    }
}