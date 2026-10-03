package com.smarturban.backend.controller;

import com.smarturban.backend.entity.*;
import com.smarturban.backend.repository.*;
import com.smarturban.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam("email") String email,
                              @RequestParam("password") String password,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        try {
            User user = userService.findByEmail(email);
            if (!passwordEncoder.matches(password, user.getPassword())) {
                model.addAttribute("error", "Invalid email or password.");
                return "admin/login";
            }
            if (!"ROLE_ADMIN".equals(user.getRole())) {
                model.addAttribute("error", "Access denied: Account is not an administrator.");
                return "admin/login";
            }
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Invalid credentials.");
            return "admin/login";
        }
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        long totalUsers = userRepository.countByRole("ROLE_CITIZEN");
        long totalComplaints = complaintRepository.count();
        long pendingComplaints = complaintRepository.countByStatus("Submitted");
        long inProgressComplaints = complaintRepository.countByStatus("In Progress");
        long resolvedComplaints = complaintRepository.countByStatus("Resolved");
        long rejectedComplaints = complaintRepository.countByStatus("Rejected");

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalComplaints", totalComplaints);
        model.addAttribute("pendingComplaints", pendingComplaints);
        model.addAttribute("inProgressComplaints", inProgressComplaints);
        model.addAttribute("resolvedComplaints", resolvedComplaints);
        model.addAttribute("rejectedComplaints", rejectedComplaints);

        List<Complaint> recentComplaints = complaintService.getAllComplaints();
        if (recentComplaints.size() > 5) {
            recentComplaints = recentComplaints.subList(0, 5);
        }
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

    @PostMapping("/users/toggle/{id}")
    public String toggleUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User user = userService.findById(id).orElseThrow();
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/complaints")
    public String complaintManagement(Model model) {
        List<Complaint> complaints = complaintService.getAllComplaints();
        List<Department> departments = departmentService.getActiveDepartments();
        model.addAttribute("complaints", complaints);
        model.addAttribute("departments", departments);
        return "admin/complaints";
    }

    @GetMapping("/complaints/{id}")
    public String complaintDetail(@PathVariable("id") Long id, Model model) {
        Complaint complaint = complaintService.getComplaintById(id);
        List<ComplaintStatusHistory> history = complaintService.getComplaintHistory(id);
        List<Department> departments = departmentService.getActiveDepartments();

        model.addAttribute("complaint", complaint);
        model.addAttribute("history", history);
        model.addAttribute("departments", departments);

        return "admin/complaint_detail";
    }

    @PostMapping("/complaints/{id}/update")
    public String updateComplaint(@PathVariable("id") Long id,
                                  @RequestParam("status") String status,
                                  @RequestParam(value = "departmentId", required = false) Long departmentId,
                                  @RequestParam(value = "remarks", required = false) String remarks,
                                  RedirectAttributes redirectAttributes) {
        complaintService.updateComplaintStatusAndDepartment(id, status, departmentId, "Admin", remarks);
        redirectAttributes.addFlashAttribute("success", "Complaint updated successfully.");
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

    @GetMapping("/categories")
    public String categoryManagement(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin/categories";
    }

    @PostMapping("/categories/add")
    public String addCategory(@RequestParam("name") String name,
                              @RequestParam("description") String description,
                              RedirectAttributes redirectAttributes) {
        categoryService.saveCategory(new Category(name, description));
        redirectAttributes.addFlashAttribute("success", "Category saved successfully.");
        return "redirect:/admin/categories";
    }
}
