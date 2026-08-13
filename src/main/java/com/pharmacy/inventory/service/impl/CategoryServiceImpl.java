package com.pharmacy.inventory.service.impl;

import com.pharmacy.inventory.dto.request.CategoryRequest;
import com.pharmacy.inventory.dto.response.CategoryResponse;
import com.pharmacy.inventory.entity.Category;
import com.pharmacy.inventory.repository.CategoryRepository;
import com.pharmacy.inventory.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return convertToResponse(category);
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        // Validate input
        if (categoryRequest.getName() == null || categoryRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }

        // Check if category with same name already exists
        if (categoryRepository.existsByName(categoryRequest.getName().trim())) {
            throw new RuntimeException("Category with name '" + categoryRequest.getName() + "' already exists");
        }

        Category category = new Category();
        category.setName(categoryRequest.getName().trim());
        category.setDescription(categoryRequest.getDescription() != null ?
                              categoryRequest.getDescription().trim() : null);

        Category savedCategory = categoryRepository.save(category);
        return convertToResponse(savedCategory);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        // Validate input
        if (categoryRequest.getName() == null || categoryRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }

        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Check if another category with the same name exists (excluding current category)
        if (categoryRepository.existsByName(categoryRequest.getName().trim()) &&
            !existingCategory.getName().equals(categoryRequest.getName().trim())) {
            throw new RuntimeException("Category with name '" + categoryRequest.getName() + "' already exists");
        }

        existingCategory.setName(categoryRequest.getName().trim());
        existingCategory.setDescription(categoryRequest.getDescription() != null ?
                                      categoryRequest.getDescription().trim() : null);

        Category updatedCategory = categoryRepository.save(existingCategory);
        return convertToResponse(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Check if category has medications before deletion
        if (category.getMedications() != null && !category.getMedications().isEmpty()) {
            throw new RuntimeException("Cannot delete category with existing medications. Please remove all medications first.");
        }

        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> searchCategories(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllCategories();
        }

        String trimmedSearchTerm = searchTerm.trim().toLowerCase();

        return categoryRepository.findAll()
                .stream()
                .filter(category ->
                    category.getName().toLowerCase().contains(trimmedSearchTerm) ||
                    (category.getDescription() != null &&
                     category.getDescription().toLowerCase().contains(trimmedSearchTerm))
                )
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse convertToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setMedicationCount(category.getMedications() != null ? category.getMedications().size() : 0);
        return response;
    }
}