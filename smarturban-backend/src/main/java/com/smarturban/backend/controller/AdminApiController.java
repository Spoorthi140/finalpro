package com.smarturban.backend.controller;

import com.smarturban.backend.entity.*;
import com.smarturban.backend.repository.*;
import com.smarturban.backend.security.UserDetailsImpl;
import com.smarturban.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<?> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.countByRole("ROLE_CITIZEN");
        long totalComplaints = complaintRepository.count();
        long pendingComplaints = complaintRepository.countByStatus("Submitted");
        long underReviewComplaints = complaintRepository.countByStatus("Under Review");
        long assignedComplaints = complaintRepository.countByStatus("Assigned");
        long inProgressComplaints = complaintRepository.countByStatus("In Progress");
        long resolvedComplaints = complaintRepository.countByStatus("Resolved");
        long rejectedComplaints = complaintRepository.countByStatus("Rejected");

        stats.put("totalUsers", totalUsers);
        stats.put("totalComplaints", totalComplaints);
        stats.put("pendingComplaints", pendingComplaints);
        stats.put("underReviewComplaints", underReviewComplaints);
        stats.put("assignedComplaints", assignedComplaints);
        stats.put("inProgressComplaints", inProgressComplaints);
        stats.put("resolvedComplaints", resolvedComplaints);
        stats.put("rejectedComplaints", rejectedComplaints);

        return ResponseEntity.ok(stats);
    }

    // User Management APIs
    @GetMapping("/users")
    public ResponseEntity<List<com.smarturban.backend.dto.UserDto>> getAllUsers() {
        List<com.smarturban.backend.dto.UserDto> dtoList = userRepository.findAll().stream()
                .map(com.smarturban.backend.dto.UserDto::new)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            user.setRole("ROLE_CITIZEN");
            User created = userService.createUserByAdmin(user);
            return ResponseEntity.ok(new com.smarturban.backend.dto.UserDto(created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @RequestBody User userDetails) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        if ("ROLE_ADMIN".equals(user.getRole()) || "admin@smarturban.com".equalsIgnoreCase(user.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot modify primary administrator roles or email"));
        }

        user.setFullName(userDetails.getFullName());
        user.setPhone(userDetails.getPhone());
        user.setAddress(userDetails.getAddress());
        user.setEnabled(userDetails.isEnabled());

        User updated = userService.saveUser(user);
        return ResponseEntity.ok(new com.smarturban.backend.dto.UserDto(updated));
    }

    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<?> toggleUserStatus(@PathVariable("id") Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        if ("ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot disable administrator accounts"));
        }
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        if ("ROLE_ADMIN".equals(user.getRole())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot delete administrator accounts"));
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    // Complaint Management APIs
    @GetMapping("/complaints")
    public ResponseEntity<List<com.smarturban.backend.dto.ComplaintResponse>> getAllComplaints() {
        List<com.smarturban.backend.dto.ComplaintResponse> responseList = complaintService.getAllComplaints().stream()
                .map(com.smarturban.backend.dto.ComplaintResponse::new)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/complaints/{id}/status")
    public ResponseEntity<?> updateComplaintStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String status = (String) body.get("status");
        Long departmentId = body.get("departmentId") != null && !body.get("departmentId").toString().isEmpty()
                ? Long.valueOf(body.get("departmentId").toString()) : null;
        String remarks = (String) body.get("remarks");

        String adminName = userDetails != null ? userDetails.getFullName() : "Administrator";

        Complaint updated = complaintService.updateComplaintStatusAndDepartment(id, status, departmentId, adminName, remarks);
        return ResponseEntity.ok(updated);
    }

    // Category Management APIs
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> saveCategory(@RequestBody Category category) {
        return ResponseEntity.ok(categoryService.saveCategory(category));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable("id") Long id, @RequestBody Category categoryDetails) {
        Category category = categoryService.getCategoryById(id);
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setEnabled(categoryDetails.isEnabled());
        return ResponseEntity.ok(categoryService.saveCategory(category));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable("id") Long id) {
        Category category = categoryService.getCategoryById(id);
        category.setEnabled(false);
        categoryService.saveCategory(category);
        return ResponseEntity.ok(Map.of("message", "Category disabled successfully"));
    }

    // Department Management APIs
    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> saveDepartment(@RequestBody Department department) {
        return ResponseEntity.ok(departmentService.saveDepartment(department));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable("id") Long id, @RequestBody Department departmentDetails) {
        Department department = departmentService.getDepartmentById(id);
        department.setName(departmentDetails.getName());
        department.setDescription(departmentDetails.getDescription());
        department.setEnabled(departmentDetails.isEnabled());
        return ResponseEntity.ok(departmentService.saveDepartment(department));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable("id") Long id) {
        Department department = departmentService.getDepartmentById(id);
        department.setEnabled(false);
        departmentService.saveDepartment(department);
        return ResponseEntity.ok(Map.of("message", "Department disabled successfully"));
    }
}
