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
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            if (user.getRole() == null) user.setRole("ROLE_CITIZEN");
            User created = userService.createUserByAdmin(user);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<?> toggleUserStatus(@PathVariable("id") Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    // Complaint Management APIs
    @GetMapping("/complaints")
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    @PutMapping("/complaints/{id}/status")
    public ResponseEntity<?> updateComplaintStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String status = (String) body.get("status");
        Long departmentId = body.get("departmentId") != null ? Long.valueOf(body.get("departmentId").toString()) : null;
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

    // Department Management APIs
    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> saveDepartment(@RequestBody Department department) {
        return ResponseEntity.ok(departmentService.saveDepartment(department));
    }
}
