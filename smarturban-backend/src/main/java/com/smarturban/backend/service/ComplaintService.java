package com.smarturban.backend.service;

import com.smarturban.backend.dto.ComplaintRequest;
import com.smarturban.backend.entity.*;
import com.smarturban.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintStatusHistoryRepository historyRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ComplaintMLClassifier mlClassifier;

    @Autowired
    private LocationRoutingEngine locationRoutingEngine;

    @Autowired
    private AIDuplicateDetector aiDuplicateDetector;

    @Transactional
    public Complaint createComplaint(User user, ComplaintRequest request, String imageUrl) {
        // 1. Run Machine Learning Text Classification
        ComplaintMLClassifier.ClassificationOutput classification =
                mlClassifier.classifyComplaint(request.getTitle(), request.getDescription());

        Category aiCategory = categoryRepository.findByName(classification.getCategoryName())
                .orElseGet(() -> categoryRepository.findByName("Other Urban Infrastructure")
                        .orElseGet(() -> categoryRepository.findAll().stream().findFirst().orElseThrow()));

        Category activeCategory = aiCategory;
        if (request.getCategoryId() != null) {
            Category userSelected = categoryRepository.findById(request.getCategoryId()).orElse(null);
            if (userSelected != null) {
                activeCategory = userSelected;
            }
        }

        // 2. Perform GPS Location & Category-Based Department Routing
        LocationRoutingEngine.RoutingResult routing = locationRoutingEngine.routeComplaint(
                aiCategory.getName(), request.getLatitude(), request.getLongitude());

        Department department = routing.getDepartment();

        // 3. AI Duplicate Complaint Detection
        AIDuplicateDetector.DuplicateDetectionResult dupResult = aiDuplicateDetector.checkForDuplicates(
                request.getTitle(), request.getDescription(), activeCategory.getId(), request.getLatitude(), request.getLongitude());

        Complaint complaint = new Complaint();
        complaint.setUser(user);
        complaint.setCategory(activeCategory);
        complaint.setDepartment(department);
        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());
        complaint.setLatitude(request.getLatitude());
        complaint.setLongitude(request.getLongitude());
        complaint.setLocationName(request.getLocationName());
        complaint.setImageUrl(imageUrl);
        complaint.setStatus("Submitted");

        // Set AI & Routing Meta Information
        complaint.setAiCategory(aiCategory);
        complaint.setAiConfidenceScore(classification.getConfidenceScore());
        complaint.setAiDepartment(department);
        complaint.setRoutingMethod(routing.getRoutingMethod());
        complaint.setIsPossibleDuplicate(dupResult.isPossibleDuplicate());
        complaint.setDuplicateSimilarityScore(dupResult.getSimilarityScore());
        complaint.setRelatedComplaint(dupResult.getRelatedComplaint());

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Record initial status history
        ComplaintStatusHistory history = new ComplaintStatusHistory(
                savedComplaint,
                null,
                "Submitted",
                user.getFullName(),
                "Complaint registered. AI categorized as '" + activeCategory.getName() + "' and routed to '" + (department != null ? department.getName() : "Unassigned") + "'."
        );
        historyRepository.save(history);

        // Create Notification
        notificationService.createNotification(
                user.getId(),
                savedComplaint.getId(),
                "Complaint Registered",
                "Your complaint '" + savedComplaint.getTitle() + "' has been submitted successfully.",
                "Complaint Submitted"
        );

        return savedComplaint;
    }

    public List<Complaint> getComplaintsByUser(Long userId) {
        return complaintRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc();
    }

    public Complaint getComplaintById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found with ID: " + id));
    }

    @Transactional
    public Complaint updateComplaintStatusAndDepartment(Long complaintId, String newStatus, Long departmentId, String changedBy, String remarks) {
        Complaint complaint = getComplaintById(complaintId);
        String oldStatus = complaint.getStatus();

        if (newStatus != null && !newStatus.trim().isEmpty()) {
            complaint.setStatus(newStatus);
        }

        if (departmentId != null) {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentId));
            if (complaint.getDepartment() == null || !complaint.getDepartment().getId().equals(departmentId)) {
                complaint.setAdminDepartmentOverridden(true);
                complaint.setRoutingMethod("MANUAL_OVERRIDE");
            }
            complaint.setDepartment(department);
        }

        Complaint updatedComplaint = complaintRepository.save(complaint);

        // Log status history
        ComplaintStatusHistory history = new ComplaintStatusHistory(
                updatedComplaint,
                oldStatus,
                complaint.getStatus(),
                changedBy,
                remarks != null ? remarks : "Status updated by admin"
        );
        historyRepository.save(history);

        // Trigger Notification on Status Update or Department Assignment
        User citizen = updatedComplaint.getUser();
        if (citizen != null) {
            String type = "Resolved".equalsIgnoreCase(updatedComplaint.getStatus()) ? "Complaint Resolved" : "Status Changed";
            String msg = "Your complaint '" + updatedComplaint.getTitle() + "' status changed to " + updatedComplaint.getStatus() + ".";
            if (remarks != null && !remarks.isEmpty()) {
                msg += " Remarks: " + remarks;
            }

            notificationService.createNotification(
                    citizen.getId(),
                    updatedComplaint.getId(),
                    "Complaint Update: " + updatedComplaint.getStatus(),
                    msg,
                    type
            );
        }

        return updatedComplaint;
    }

    public List<ComplaintStatusHistory> getComplaintHistory(Long complaintId) {
        return historyRepository.findByComplaintIdOrderByChangedAtAsc(complaintId);
    }

    @Transactional
    public Complaint adminOverrideCategory(Long complaintId, Long newCategoryId) {
        Complaint complaint = getComplaintById(complaintId);
        Category newCategory = categoryRepository.findById(newCategoryId)
                .orElseThrow(() -> new RuntimeException("Category not found ID: " + newCategoryId));
        complaint.setCategory(newCategory);
        complaint.setAdminCategoryOverridden(true);
        return complaintRepository.save(complaint);
    }

    @Transactional
    public Complaint adminReviewDuplicate(Long complaintId, boolean isValidDuplicate) {
        Complaint complaint = getComplaintById(complaintId);
        complaint.setDuplicateReviewed(true);
        complaint.setIsValidDuplicate(isValidDuplicate);
        if (isValidDuplicate) {
            complaint.setStatus("Rejected"); // Automatically flag confirmed duplicate as resolved/rejected
        } else {
            complaint.setIsPossibleDuplicate(false); // Clear duplicate warning if dismissed
        }
        return complaintRepository.save(complaint);
    }
}
