package com.smarturban.backend.dto;

import com.smarturban.backend.entity.Complaint;
import java.time.LocalDateTime;

public class ComplaintResponse {

    public static class IdNameDto {
        private Long id;
        private String name;

        public IdNameDto() {}
        public IdNameDto(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    private Long id;
    private String title;
    private String description;
    private String status;
    private IdNameDto category;
    private IdNameDto department;
    private Double latitude;
    private Double longitude;
    private String locationName;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private IdNameDto aiCategory;
    private Double aiConfidenceScore;
    private IdNameDto aiDepartment;
    private String routingMethod;
    private Boolean isPossibleDuplicate;
    private Double duplicateSimilarityScore;
    private Boolean adminCategoryOverridden;
    private Boolean adminDepartmentOverridden;

    public ComplaintResponse() {}

    public ComplaintResponse(Complaint c) {
        if (c != null) {
            this.id = c.getId();
            this.title = c.getTitle();
            this.description = c.getDescription();
            this.status = c.getStatus();

            if (c.getCategory() != null) {
                this.category = new IdNameDto(c.getCategory().getId(), c.getCategory().getName());
            }
            if (c.getDepartment() != null) {
                this.department = new IdNameDto(c.getDepartment().getId(), c.getDepartment().getName());
            }

            this.latitude = c.getLatitude();
            this.longitude = c.getLongitude();
            this.locationName = c.getLocationName();
            this.imageUrl = c.getImageUrl();
            this.createdAt = c.getCreatedAt();
            this.updatedAt = c.getUpdatedAt();

            if (c.getAiCategory() != null) {
                this.aiCategory = new IdNameDto(c.getAiCategory().getId(), c.getAiCategory().getName());
            }
            this.aiConfidenceScore = c.getAiConfidenceScore();
            if (c.getAiDepartment() != null) {
                this.aiDepartment = new IdNameDto(c.getAiDepartment().getId(), c.getAiDepartment().getName());
            }
            this.routingMethod = c.getRoutingMethod();
            this.isPossibleDuplicate = c.getIsPossibleDuplicate();
            this.duplicateSimilarityScore = c.getDuplicateSimilarityScore();
            this.adminCategoryOverridden = c.getAdminCategoryOverridden();
            this.adminDepartmentOverridden = c.getAdminDepartmentOverridden();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public IdNameDto getCategory() { return category; }
    public void setCategory(IdNameDto category) { this.category = category; }

    public IdNameDto getDepartment() { return department; }
    public void setDepartment(IdNameDto department) { this.department = department; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public IdNameDto getAiCategory() { return aiCategory; }
    public void setAiCategory(IdNameDto aiCategory) { this.aiCategory = aiCategory; }

    public Double getAiConfidenceScore() { return aiConfidenceScore; }
    public void setAiConfidenceScore(Double aiConfidenceScore) { this.aiConfidenceScore = aiConfidenceScore; }

    public IdNameDto getAiDepartment() { return aiDepartment; }
    public void setAiDepartment(IdNameDto aiDepartment) { this.aiDepartment = aiDepartment; }

    public String getRoutingMethod() { return routingMethod; }
    public void setRoutingMethod(String routingMethod) { this.routingMethod = routingMethod; }

    public Boolean getIsPossibleDuplicate() { return isPossibleDuplicate; }
    public void setIsPossibleDuplicate(Boolean possibleDuplicate) { isPossibleDuplicate = possibleDuplicate; }

    public Double getDuplicateSimilarityScore() { return duplicateSimilarityScore; }
    public void setDuplicateSimilarityScore(Double duplicateSimilarityScore) { this.duplicateSimilarityScore = duplicateSimilarityScore; }

    public Boolean getAdminCategoryOverridden() { return adminCategoryOverridden; }
    public void setAdminCategoryOverridden(Boolean adminCategoryOverridden) { this.adminCategoryOverridden = adminCategoryOverridden; }

    public Boolean getAdminDepartmentOverridden() { return adminDepartmentOverridden; }
    public void setAdminDepartmentOverridden(Boolean adminDepartmentOverridden) { this.adminDepartmentOverridden = adminDepartmentOverridden; }
}
