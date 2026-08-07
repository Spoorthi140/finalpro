package com.carebeforecare.prehospitalcare_platform.model.dto;

import java.time.LocalDateTime;

public class HealthAssessmentDTO {

    private Long id;
    private String userId;
    private Integer age;
    private String gender;
    private Double temperature;
    private Integer heartRate;
    private Boolean chestPain;
    private Boolean difficultyBreathing;
    private Boolean bleeding;
    private Boolean unconscious;
    private Boolean vomiting;
    private Boolean headache;
    private Boolean diabetes;
    private Boolean bloodPressure;
    private Boolean pregnant; // Pregnant status is part of the form but not explicitly specified in the database requirements (or we can include/save it, but the database list in instructions didn't list it. Let's include it in the DTO for flow).
    private Integer riskScore;
    private String riskLevel;
    private String recommendation;
    private LocalDateTime assessmentDate;

    public HealthAssessmentDTO() {}

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

    public Boolean getPregnant() { return pregnant; }
    public void setPregnant(Boolean pregnant) { this.pregnant = pregnant; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public LocalDateTime getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(LocalDateTime assessmentDate) { this.assessmentDate = assessmentDate; }
}