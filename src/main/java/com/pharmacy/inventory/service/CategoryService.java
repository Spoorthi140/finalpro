package com.pharmacy.inventory.service;

import com.pharmacy.inventory.dto.request.CategoryRequest;
import com.pharmacy.inventory.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse createCategory(CategoryRequest categoryRequest);

    CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest);

    void deleteCategory(Long id);

    List<CategoryResponse> searchCategories(String searchTerm);
}