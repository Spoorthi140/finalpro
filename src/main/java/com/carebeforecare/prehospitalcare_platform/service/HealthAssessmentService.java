package com.carebeforecare.prehospitalcare_platform.service;

import com.carebeforecare.prehospitalcare_platform.model.dto.HealthAssessmentDTO;
import com.carebeforecare.prehospitalcare_platform.model.entity.HealthAssessment;

import java.util.List;
import java.util.Optional;

public interface HealthAssessmentService {
    HealthAssessment evaluateAndSave(HealthAssessmentDTO assessmentDTO);
    List<HealthAssessment> getAllAssessments();
    List<HealthAssessment> searchAssessments(String keyword);
    Optional<HealthAssessment> getAssessmentById(Long id);
    long countAssessments();
    long countByRiskLevel(String riskLevel);
    List<HealthAssessment> getRecentAssessments(int limit);
}