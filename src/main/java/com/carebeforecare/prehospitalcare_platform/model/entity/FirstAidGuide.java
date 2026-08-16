package com.carebeforecare.prehospitalcare_platform.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "first_aid_guides")
public class FirstAidGuide {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String category;

    @Column(nullable = false)
    private String difficulty = "EASY";

    @Column(nullable = false)
    private String severity = "MEDIUM";

    @Column(nullable = false)
    private String priority = "MEDIUM";

    private String estimatedTime;
    private String equipmentNeeded;
    private String icon;

    @Column(length = 2000)
    private String description;

    @Column(length = 4000)
    private String steps;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public FirstAidGuide() {}

    public FirstAidGuide(String title, String category, String steps) {
        this.title = title;
        this.category = category;
        this.steps = steps;
        this.difficulty = "EASY";
        this.severity = "MEDIUM";
        this.priority = "MEDIUM";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty != null ? difficulty : "EASY"; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity != null ? severity : "MEDIUM"; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority != null ? priority : "MEDIUM"; }
    public String getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }
    public String getEquipmentNeeded() { return equipmentNeeded; }
    public void setEquipmentNeeded(String equipmentNeeded) { this.equipmentNeeded = equipmentNeeded; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Ensure default values
        if (this.difficulty == null) this.difficulty = "EASY";
        if (this.severity == null) this.severity = "MEDIUM";
        if (this.priority == null) this.priority = "MEDIUM";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}