package com.carebeforecare.prehospitalcare_platform.service.impl;

import com.carebeforecare.prehospitalcare_platform.model.dto.HealthAssessmentDTO;
import com.carebeforecare.prehospitalcare_platform.model.entity.HealthAssessment;
import com.carebeforecare.prehospitalcare_platform.repository.HealthAssessmentRepository;
import com.carebeforecare.prehospitalcare_platform.service.HealthAssessmentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HealthAssessmentServiceImpl implements HealthAssessmentService {

    private final HealthAssessmentRepository repository;

    public HealthAssessmentServiceImpl(HealthAssessmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public HealthAssessment evaluateAndSave(HealthAssessmentDTO dto) {
        int score = 0;

        // Calculate risk score based on rule-based logic
        if (dto.getChestPain() != null && dto.getChestPain()) {
            score += 3;
        }
        if (dto.getDifficultyBreathing() != null && dto.getDifficultyBreathing()) {
            score += 3;
        }
        if (dto.getUnconscious() != null && dto.getUnconscious()) {
            score += 5;
        }
        if (dto.getBleeding() != null && dto.getBleeding()) {
            score += 5;
        }
        if (dto.getVomiting() != null && dto.getVomiting()) {
            score += 2;
        }
        if (dto.getTemperature() != null && dto.getTemperature() > 39.0) {
            score += 2;
        }
        if (dto.getHeartRate() != null && dto.getHeartRate() > 120) {
            score += 2;
        }
        if (dto.getAge() != null && dto.getAge() > 65) {
            score += 2;
        }
        if (dto.getDiabetes() != null && dto.getDiabetes()) {
            score += 1;
        }
        if (dto.getBloodPressure() != null && dto.getBloodPressure()) {
            score += 1;
        }

        String level;
        String recommendation;

        if (score <= 3) {
            level = "LOW RISK";
            recommendation = "Your symptoms appear mild. Continue monitoring your condition. Rest, stay hydrated, and seek medical advice if symptoms worsen.";
        } else if (score <= 7) {
            level = "MODERATE RISK";
            recommendation = "Medical consultation is recommended. Visit a nearby clinic or healthcare provider.";
        } else {
            level = "HIGH RISK";
            recommendation = "Immediate medical attention is strongly recommended. Please visit the nearest hospital or contact emergency services.";
        }

        // Map DTO to Entity
        HealthAssessment entity = new HealthAssessment();
        entity.setUserId(dto.getUserId() != null ? dto.getUserId() : "GUEST");
        entity.setAge(dto.getAge());
        entity.setGender(dto.getGender());
        entity.setTemperature(dto.getTemperature());
        entity.setHeartRate(dto.getHeartRate());
        entity.setChestPain(dto.getChestPain() != null ? dto.getChestPain() : false);
        entity.setDifficultyBreathing(dto.getDifficultyBreathing() != null ? dto.getDifficultyBreathing() : false);
        entity.setBleeding(dto.getBleeding() != null ? dto.getBleeding() : false);
        entity.setUnconscious(dto.getUnconscious() != null ? dto.getUnconscious() : false);
        entity.setVomiting(dto.getVomiting() != null ? dto.getVomiting() : false);
        entity.setHeadache(dto.getHeadache() != null ? dto.getHeadache() : false);
        entity.setDiabetes(dto.getDiabetes() != null ? dto.getDiabetes() : false);
        entity.setBloodPressure(dto.getBloodPressure() != null ? dto.getBloodPressure() : false);
        entity.setRiskScore(score);
        entity.setRiskLevel(level);
        entity.setRecommendation(recommendation);
        entity.setAssessmentDate(LocalDateTime.now());

        return repository.save(entity);
    }

    @Override
    public List<HealthAssessment> getAllAssessments() {
        return repository.findAllByOrderByAssessmentDateDesc();
    }

    @Override
    public List<HealthAssessment> searchAssessments(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllAssessments();
        }
        return repository.searchAssessments(keyword);
    }

    @Override
    public Optional<HealthAssessment> getAssessmentById(Long id) {
        return repository.findById(id);
    }

    @Override
    public long countAssessments() {
        return repository.count();
    }

    @Override
    public long countByRiskLevel(String riskLevel) {
        return repository.countByRiskLevel(riskLevel);
    }

    @Override
    public List<HealthAssessment> getRecentAssessments(int limit) {
        return repository.findAllByOrderByAssessmentDateDesc().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}