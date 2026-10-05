package com.smarturban.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private Double latitude;

    private Double longitude;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private String status = "Submitted"; // Submitted, Under Review, Assigned, In Progress, Resolved, Rejected

    // AI & ML Complaint Intelligence Fields
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ai_category_id")
    private Category aiCategory;

    @Column(name = "ai_confidence_score")
    private Double aiConfidenceScore;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ai_department_id")
    private Department aiDepartment;

    @Column(name = "routing_method")
    private String routingMethod; // "LOCATION_BASED", "CATEGORY_BASED", "MANUAL_OVERRIDE"

    @Column(name = "is_possible_duplicate")
    private Boolean isPossibleDuplicate = false;

    @Column(name = "duplicate_similarity_score")
    private Double duplicateSimilarityScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_complaint_id")
    private Complaint relatedComplaint;

    @Column(name = "admin_category_overridden")
    private Boolean adminCategoryOverridden = false;

    @Column(name = "admin_department_overridden")
    private Boolean adminDepartmentOverridden = false;

    @Column(name = "duplicate_reviewed")
    private Boolean duplicateReviewed = false;

    @Column(name = "is_valid_duplicate")
    private Boolean isValidDuplicate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Complaint() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Category getAiCategory() { return aiCategory; }
    public void setAiCategory(Category aiCategory) { this.aiCategory = aiCategory; }

    public Double getAiConfidenceScore() { return aiConfidenceScore; }
    public void setAiConfidenceScore(Double aiConfidenceScore) { this.aiConfidenceScore = aiConfidenceScore; }

    public Department getAiDepartment() { return aiDepartment; }
    public void setAiDepartment(Department aiDepartment) { this.aiDepartment = aiDepartment; }

    public String getRoutingMethod() { return routingMethod; }
    public void setRoutingMethod(String routingMethod) { this.routingMethod = routingMethod; }

    public Boolean getIsPossibleDuplicate() { return isPossibleDuplicate; }
    public void setIsPossibleDuplicate(Boolean possibleDuplicate) { isPossibleDuplicate = possibleDuplicate; }

    public Double getDuplicateSimilarityScore() { return duplicateSimilarityScore; }
    public void setDuplicateSimilarityScore(Double duplicateSimilarityScore) { this.duplicateSimilarityScore = duplicateSimilarityScore; }

    public Complaint getRelatedComplaint() { return relatedComplaint; }
    public void setRelatedComplaint(Complaint relatedComplaint) { this.relatedComplaint = relatedComplaint; }

    public Boolean getAdminCategoryOverridden() { return adminCategoryOverridden; }
    public void setAdminCategoryOverridden(Boolean adminCategoryOverridden) { this.adminCategoryOverridden = adminCategoryOverridden; }

    public Boolean getAdminDepartmentOverridden() { return adminDepartmentOverridden; }
    public void setAdminDepartmentOverridden(Boolean adminDepartmentOverridden) { this.adminDepartmentOverridden = adminDepartmentOverridden; }

    public Boolean getDuplicateReviewed() { return duplicateReviewed; }
    public void setDuplicateReviewed(Boolean duplicateReviewed) { this.duplicateReviewed = duplicateReviewed; }

    public Boolean getIsValidDuplicate() { return isValidDuplicate; }
    public void setIsValidDuplicate(Boolean validDuplicate) { isValidDuplicate = validDuplicate; }
}
