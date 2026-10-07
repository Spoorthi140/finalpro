package com.smarturban.backend.controller;

import com.smarturban.backend.entity.*;
import com.smarturban.backend.repository.*;
import com.smarturban.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

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

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        long totalUsers = userRepository.countByRole("ROLE_CITIZEN");
        long totalComplaints = complaintRepository.count();
        long pendingComplaints = complaintRepository.countByStatus("Submitted");
        long inProgressComplaints = complaintRepository.countByStatus("In Progress");
        long resolvedComplaints = complaintRepository.countByStatus("Resolved");
        long rejectedComplaints = complaintRepository.countByStatus("Rejected");

        long resolutionPercentage = totalComplaints > 0 ? Math.round((double) resolvedComplaints / totalComplaints * 100.0) : 0;

        List<Complaint> allComplaints = complaintService.getAllComplaints();

        // Grouping by Category
        java.util.Map<String, Long> categoryStats = allComplaints.stream()
                .filter(c -> c.getCategory() != null)
                .collect(java.util.stream.Collectors.groupingBy(c -> c.getCategory().getName(), java.util.stream.Collectors.counting()));

        // Grouping by Department
        java.util.Map<String, Long> departmentStats = allComplaints.stream()
                .filter(c -> c.getDepartment() != null)
                .collect(java.util.stream.Collectors.groupingBy(c -> c.getDepartment().getName(), java.util.stream.Collectors.counting()));

        // Monthly Trend
        java.util.Map<String, Long> monthlyTrend = allComplaints.stream()
                .filter(c -> c.getCreatedAt() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        c -> c.getCreatedAt().getMonth().name().substring(0, 3) + " " + c.getCreatedAt().getYear(),
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.counting()
                ));

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalComplaints", totalComplaints);
        model.addAttribute("pendingComplaints", pendingComplaints);
        model.addAttribute("inProgressComplaints", inProgressComplaints);
        model.addAttribute("resolvedComplaints", resolvedComplaints);
        model.addAttribute("rejectedComplaints", rejectedComplaints);
        model.addAttribute("resolutionPercentage", resolutionPercentage);
        model.addAttribute("categoryStats", categoryStats);
        model.addAttribute("departmentStats", departmentStats);
        model.addAttribute("monthlyTrend", monthlyTrend);

        List<Complaint> recentComplaints = allComplaints.size() > 5 ? allComplaints.subList(0, 5) : allComplaints;
        model.addAttribute("recentComplaints", recentComplaints);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userManagement(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/users/add")
    public String addUser(@RequestParam("fullName") String fullName,
                          @RequestParam("email") String email,
                          @RequestParam("phone") String phone,
                          @RequestParam("password") String password,
                          @RequestParam("address") String address,
                          @RequestParam("role") String role,
                          RedirectAttributes redirectAttributes) {
        try {
            User user = new User(fullName, email, phone, password, address, role);
            userService.createUserByAdmin(user);
            redirectAttributes.addFlashAttribute("success", "User added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/edit/{id}")
    public String editUser(@PathVariable("id") Long id,
                           @RequestParam("fullName") String fullName,
                           @RequestParam("email") String email,
                           @RequestParam("phone") String phone,
                           @RequestParam("address") String address,
                           @RequestParam("role") String role,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

            if ("admin@smarturban.com".equalsIgnoreCase(user.getEmail()) && !"admin@smarturban.com".equalsIgnoreCase(email)) {
                redirectAttributes.addFlashAttribute("error", "Cannot change primary administrator email address.");
                return "redirect:/admin/users";
            }

            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            user.setAddress(address);
            user.setRole(role);

            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "User details updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/toggle/{id}")
    public String toggleUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User user = userService.findById(id).orElseThrow();
        if ("ROLE_ADMIN".equals(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Cannot disable administrator accounts.");
            return "redirect:/admin/users";
        }
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id).orElseThrow();
            if ("ROLE_ADMIN".equals(user.getRole())) {
                redirectAttributes.addFlashAttribute("error", "Cannot delete administrator accounts.");
                return "redirect:/admin/users";
            }
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/complaints")
    public String complaintManagement(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Model model) {

        List<Complaint> complaints = complaintService.searchAndFilterComplaints(query, status, categoryId, departmentId, startDate, endDate);
        List<Department> departments = departmentService.getActiveDepartments();
        List<Category> categories = categoryService.getActiveCategories();

        model.addAttribute("complaints", complaints);
        model.addAttribute("departments", departments);
        model.addAttribute("categories", categories);

        model.addAttribute("paramQuery", query);
        model.addAttribute("paramStatus", status);
        model.addAttribute("paramCategoryId", categoryId);
        model.addAttribute("paramDepartmentId", departmentId);
        model.addAttribute("paramStartDate", startDate);
        model.addAttribute("paramEndDate", endDate);

        return "admin/complaints";
    }

    @GetMapping("/complaints/{id}")
    public String complaintDetail(@PathVariable("id") Long id, Model model) {
        Complaint complaint = complaintService.getComplaintById(id);
        List<ComplaintStatusHistory> history = complaintService.getComplaintHistory(id);
        List<Department> departments = departmentService.getActiveDepartments();
        List<Category> categories = categoryService.getActiveCategories();
        Optional<ComplaintFeedback> feedback = feedbackService.getFeedbackForComplaint(id);

        model.addAttribute("complaint", complaint);
        model.addAttribute("history", history);
        model.addAttribute("departments", departments);
        model.addAttribute("categories", categories);
        model.addAttribute("feedback", feedback.orElse(null));

        return "admin/complaint_detail";
    }

    @PostMapping("/complaints/{id}/update")
    public String updateComplaint(@PathVariable("id") Long id,
                                  @RequestParam("status") String status,
                                  @RequestParam(value = "departmentId", required = false) Long departmentId,
                                  @RequestParam(value = "remarks", required = false) String remarks,
                                  RedirectAttributes redirectAttributes) {
        complaintService.updateComplaintStatusAndDepartment(id, status, departmentId, "Administrator", remarks);
        redirectAttributes.addFlashAttribute("success", "Complaint updated successfully.");
        return "redirect:/admin/complaints/" + id;
    }

    @PostMapping("/complaints/{id}/override-category")
    public String overrideCategory(@PathVariable("id") Long id,
                                   @RequestParam("categoryId") Long categoryId,
                                   RedirectAttributes redirectAttributes) {
        complaintService.adminOverrideCategory(id, categoryId);
        redirectAttributes.addFlashAttribute("success", "Category updated by Admin successfully.");
        return "redirect:/admin/complaints/" + id;
    }

    @PostMapping("/complaints/{id}/review-duplicate")
    public String reviewDuplicate(@PathVariable("id") Long id,
                                  @RequestParam("isValidDuplicate") boolean isValidDuplicate,
                                  RedirectAttributes redirectAttributes) {
        complaintService.adminReviewDuplicate(id, isValidDuplicate);
        String msg = isValidDuplicate ? "Complaint marked as Confirmed Duplicate for administrative review." : "Duplicate warning dismissed.";
        redirectAttributes.addFlashAttribute("success", msg);
        return "redirect:/admin/complaints/" + id;
    }

    @GetMapping("/departments")
    public String departmentManagement(Model model) {
        List<Department> departments = departmentService.getAllDepartments();
        model.addAttribute("departments", departments);
        return "admin/departments";
    }

    @PostMapping("/departments/add")
    public String addDepartment(@RequestParam("name") String name,
                                @RequestParam("description") String description,
                                RedirectAttributes redirectAttributes) {
        departmentService.saveDepartment(new Department(name, description));
        redirectAttributes.addFlashAttribute("success", "Department saved successfully.");
        return "redirect:/admin/departments";
    }

    @PostMapping("/departments/edit/{id}")
    public String editDepartment(@PathVariable("id") Long id,
                                 @RequestParam("name") String name,
                                 @RequestParam("description") String description,
                                 RedirectAttributes redirectAttributes) {
        try {
            Department department = departmentService.getDepartmentById(id);
            department.setName(name);
            department.setDescription(description);
            departmentService.saveDepartment(department);
            redirectAttributes.addFlashAttribute("success", "Department updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    @PostMapping("/departments/delete/{id}")
    public String deleteDepartment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            departmentService.deleteOrDeactivateDepartment(id);
            redirectAttributes.addFlashAttribute("success", "Department deactivated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    @GetMapping("/categories")
    public String categoryManagement(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Department> departments = departmentService.getActiveDepartments();
        model.addAttribute("categories", categories);
        model.addAttribute("departments", departments);
        return "admin/categories";
    }

    @PostMapping("/categories/add")
    public String addCategory(@RequestParam("name") String name,
                              @RequestParam("description") String description,
                              @RequestParam(value = "defaultDepartmentId", required = false) Long defaultDepartmentId,
                              RedirectAttributes redirectAttributes) {
        Department defaultDept = null;
        if (defaultDepartmentId != null) {
            defaultDept = departmentService.getDepartmentById(defaultDepartmentId);
        }
        categoryService.saveCategory(new Category(name, description, defaultDept));
        redirectAttributes.addFlashAttribute("success", "Category saved successfully.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable("id") Long id,
                               @RequestParam("name") String name,
                               @RequestParam("description") String description,
                               @RequestParam(value = "defaultDepartmentId", required = false) Long defaultDepartmentId,
                               RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.getCategoryById(id);
            category.setName(name);
            category.setDescription(description);
            if (defaultDepartmentId != null) {
                Department defaultDept = departmentService.getDepartmentById(defaultDepartmentId);
                category.setDefaultDepartment(defaultDept);
            } else {
                category.setDefaultDepartment(null);
            }
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("success", "Category updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteOrDeactivateCategory(id);
            redirectAttributes.addFlashAttribute("success", "Category deactivated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/feedback")
    public String feedbackManagement(Model model) {
        List<ComplaintFeedback> feedbackList = feedbackService.getAllFeedback();
        double avgRating = feedbackList.stream()
                .mapToInt(ComplaintFeedback::getRating)
                .average()
                .orElse(0.0);

        model.addAttribute("feedbackList", feedbackList);
        model.addAttribute("totalFeedback", feedbackList.size());
        model.addAttribute("avgRating", avgRating);
        return "admin/feedback";
    }
}
