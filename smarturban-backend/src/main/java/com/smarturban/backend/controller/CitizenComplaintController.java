package com.smarturban.backend.controller;

import com.smarturban.backend.dto.ComplaintRequest;
import com.smarturban.backend.entity.Complaint;
import com.smarturban.backend.entity.ComplaintFeedback;
import com.smarturban.backend.entity.ComplaintStatusHistory;
import com.smarturban.backend.entity.User;
import com.smarturban.backend.security.UserDetailsImpl;
import com.smarturban.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/citizen")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CitizenComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ComplaintMLClassifier mlClassifier;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("fullName", user.getFullName());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        profile.put("address", user.getAddress());
        profile.put("role", user.getRole());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @RequestBody Map<String, String> request) {
        User updated = userService.updateProfile(
                userDetails.getId(),
                request.get("fullName"),
                request.get("phone"),
                request.get("address")
        );
        return ResponseEntity.ok(updated);
    }

    @PostMapping(value = "/complaints", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> submitComplaint(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "locationName", required = false) String locationName,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        User user = userService.findByEmail(userDetails.getUsername());

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = fileStorageService.storeFile(image);
        }

        ComplaintRequest request = new ComplaintRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategoryId(categoryId);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setLocationName(locationName);

        Complaint complaint = complaintService.createComplaint(user, request, imageUrl);

        return ResponseEntity.ok(complaint);
    }

    @PostMapping("/complaints/ai-recommend-category")
    public ResponseEntity<?> recommendCategory(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String description = request.get("description");
        ComplaintMLClassifier.ClassificationOutput result = mlClassifier.classifyComplaint(title, description);

        Long suggestedCategoryId = categoryService.getActiveCategories().stream()
                .filter(c -> c.getName().equalsIgnoreCase(result.getCategoryName()))
                .map(c -> c.getId())
                .findFirst()
                .orElse(null);

        Map<String, Object> response = new HashMap<>();
        response.put("suggestedCategoryId", suggestedCategoryId);
        response.put("confidenceScore", result.getConfidenceScore());
        response.put("lowConfidence", result.isLowConfidence());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/complaints")
    public ResponseEntity<List<Complaint>> getMyComplaints(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<Complaint> complaints = complaintService.getComplaintsByUser(userDetails.getId());
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/complaints/{id}")
    public ResponseEntity<?> getComplaintDetails(@PathVariable("id") Long id,
                                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Complaint complaint = complaintService.getComplaintById(id);
        if (!complaint.getUser().getId().equals(userDetails.getId()) && !"ROLE_ADMIN".equals(userDetails.getAuthorities().iterator().next().getAuthority())) {
            return ResponseEntity.status(403).body("Access Denied");
        }
        return ResponseEntity.ok(complaint);
    }

    @GetMapping("/complaints/{id}/history")
    public ResponseEntity<List<ComplaintStatusHistory>> getComplaintHistory(@PathVariable("id") Long id) {
        return ResponseEntity.ok(complaintService.getComplaintHistory(id));
    }

    @PostMapping("/complaints/{id}/feedback")
    public ResponseEntity<?> submitFeedback(@PathVariable("id") Long id,
                                           @RequestBody Map<String, Object> request,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            User citizen = userService.findByEmail(userDetails.getUsername());
            Integer rating = request.get("rating") != null ? Integer.valueOf(request.get("rating").toString()) : null;
            String comments = (String) request.get("comments");

            ComplaintFeedback feedback = feedbackService.submitFeedback(citizen, id, rating, comments);
            return ResponseEntity.ok(feedback);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/complaints/{id}/feedback")
    public ResponseEntity<?> getFeedback(@PathVariable("id") Long id) {
        Optional<ComplaintFeedback> feedback = feedbackService.getFeedbackForComplaint(id);
        return feedback.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
