package com.smarturban.backend.service;

import com.smarturban.backend.entity.Category;
import com.smarturban.backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getActiveCategories() {
        return categoryRepository.findByEnabledTrue();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
    }

    public Category saveCategory(Category category) {
        if (category.getId() == null && category.getName() != null) {
            boolean exists = categoryRepository.findAll().stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(category.getName().trim()));
            if (exists) {
                throw new RuntimeException("A category with the name '" + category.getName() + "' already exists.");
            }
        }
        return categoryRepository.save(category);
    }

    public void deleteOrDeactivateCategory(Long id) {
        Category category = getCategoryById(id);
        category.setEnabled(false);
        categoryRepository.save(category);
    }
}
