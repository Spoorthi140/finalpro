package com.smarturban.backend.controller;

import com.smarturban.backend.entity.Category;
import com.smarturban.backend.entity.Department;
import com.smarturban.backend.service.CategoryService;
import com.smarturban.backend.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CategoryDepartmentController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.getActiveCategories());
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getActiveDepartments() {
        return ResponseEntity.ok(departmentService.getActiveDepartments());
    }
}
