package com.example.board.service;

import com.example.board.domain.Category;
import com.example.board.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // 카테고리 생성
    public Long createCategory(String name) {
        Category category = new Category(name);
        Category savedCategory = categoryRepository.save(category);
        return savedCategory.getId();
    }
}