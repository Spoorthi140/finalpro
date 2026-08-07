package com.carebeforecare.prehospitalcare_platform.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_assessments")
public class HealthAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId; // to handle possible user tracking if required, defaults to guest or simple id

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private Double temperature;

    @Column(name = "heart_rate", nullable = false)
    private Integer heartRate;

    @Column(name = "chest_pain", nullable = false)
    private Boolean chestPain;

    @Column(name = "difficulty_breathing", nullable = false)
    private Boolean difficultyBreathing;

    @Column(nullable = false)
    private Boolean bleeding;

    @Column(nullable = false)
    private Boolean unconscious;

    @Column(nullable = false)
    private Boolean vomiting;

    @Column(nullable = false)
    private Boolean headache;

    @Column(nullable = false)
    private Boolean diabetes;

    @Column(name = "blood_pressure", nullable = false)
    private Boolean bloodPressure;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Column(name = "risk_level", nullable = false)
    private String riskLevel;

    @Column(length = 1000, nullable = false)
    private String recommendation;

    @Column(name = "assessment_date", nullable = false)
    private LocalDateTime assessmentDate;

    public HealthAssessment() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }

    public Boolean getChestPain() { return chestPain; }
    public void setChestPain(Boolean chestPain) { this.chestPain = chestPain; }

    public Boolean getDifficultyBreathing() { return difficultyBreathing; }
    public void setDifficultyBreathing(Boolean difficultyBreathing) { this.difficultyBreathing = difficultyBreathing; }

    public Boolean getBleeding() { return bleeding; }
    public void setBleeding(Boolean bleeding) { this.bleeding = bleeding; }

    public Boolean getUnconscious() { return unconscious; }
    public void setUnconscious(Boolean unconscious) { this.unconscious = unconscious; }

    public Boolean getVomiting() { return vomiting; }
    public void setVomiting(Boolean vomiting) { this.vomiting = vomiting; }

    public Boolean getHeadache() { return headache; }
    public void setHeadache(Boolean headache) { this.headache = headache; }

    public Boolean getDiabetes() { return diabetes; }
    public void setDiabetes(Boolean diabetes) { this.diabetes = diabetes; }

    public Boolean getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(Boolean bloodPressure) { this.bloodPressure = bloodPressure; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public LocalDateTime getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(LocalDateTime assessmentDate) { this.assessmentDate = assessmentDate; }

    @PrePersist
    protected void onCreate() {
        if (assessmentDate == null) {
            assessmentDate = LocalDateTime.now();
        }
    }
}