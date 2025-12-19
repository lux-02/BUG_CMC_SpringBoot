package com.example.board.controller;

import com.example.board.service.CategoryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // [카테고리 생성] POST http://localhost:8080/categories?name=자유게시판
    @PostMapping(value = "/categories", produces = "text/plain;charset=UTF-8")
    public String createCategory(@RequestParam String name) {
        Long categoryId = categoryService.createCategory(name);
        return "카테고리 생성 완료! ID: " + categoryId;
    }
}