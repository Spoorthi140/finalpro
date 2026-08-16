package com.carebeforecare.prehospitalcare_platform.repository;

import com.carebeforecare.prehospitalcare_platform.model.entity.HealthAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthAssessmentRepository extends JpaRepository<HealthAssessment, Long> {

    @Query("SELECT h FROM HealthAssessment h ORDER BY h.assessmentDate DESC")
    List<HealthAssessment> findAllByOrderByAssessmentDateDesc();

    @Query("SELECT h FROM HealthAssessment h WHERE " +
           "LOWER(h.gender) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(h.riskLevel) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(h.recommendation) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY h.assessmentDate DESC")
    List<HealthAssessment> searchAssessments(@Param("keyword") String keyword);

    long countByRiskLevel(String riskLevel);

    @Query("SELECT h FROM HealthAssessment h ORDER BY h.assessmentDate DESC")
    List<HealthAssessment> findRecentAssessments();
}