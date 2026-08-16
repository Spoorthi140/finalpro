package com.carebeforecare.prehospitalcare_platform.service.impl;

import com.carebeforecare.prehospitalcare_platform.model.entity.FirstAidGuide;
import com.carebeforecare.prehospitalcare_platform.repository.FirstAidGuideRepository;
import com.carebeforecare.prehospitalcare_platform.service.FirstAidService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FirstAidServiceImpl implements FirstAidService {

    private final FirstAidGuideRepository firstAidGuideRepository;

    public FirstAidServiceImpl(FirstAidGuideRepository firstAidGuideRepository) {
        this.firstAidGuideRepository = firstAidGuideRepository;
    }

    @Override
    public List<FirstAidGuide> getAllFirstAidGuides() {
        return firstAidGuideRepository.findAll();
    }

    @Override
    public Optional<FirstAidGuide> getFirstAidGuideById(Long id) {
        return firstAidGuideRepository.findById(id);
    }

    @Override
    public List<FirstAidGuide> getFirstAidGuidesByCategory(String category) {
        return firstAidGuideRepository.findByCategory(category);
    }

    @Override
    public List<FirstAidGuide> searchFirstAidGuides(String keyword) {
        return firstAidGuideRepository.searchFirstAidGuides(keyword);
    }

    @Override
    public List<FirstAidGuide> getRelatedFirstAidGuides(String category, Long excludeId) {
        return firstAidGuideRepository.findByCategoryAndIdNot(category, excludeId);
    }

    @Override
    public FirstAidGuide saveFirstAidGuide(FirstAidGuide guide) {
        return firstAidGuideRepository.save(guide);
    }

    @Override
    public FirstAidGuide updateFirstAidGuide(Long id, FirstAidGuide guideDetails) {
        FirstAidGuide existingGuide = firstAidGuideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("First aid guide not found with id: " + id));

        existingGuide.setTitle(guideDetails.getTitle());
        existingGuide.setDescription(guideDetails.getDescription());
        existingGuide.setCategory(guideDetails.getCategory());
        existingGuide.setSeverity(guideDetails.getSeverity());
        existingGuide.setDifficulty(guideDetails.getDifficulty());
        existingGuide.setEstimatedTime(guideDetails.getEstimatedTime());
        existingGuide.setEquipmentNeeded(guideDetails.getEquipmentNeeded());
        existingGuide.setSteps(guideDetails.getSteps());
        existingGuide.setIcon(guideDetails.getIcon());
        existingGuide.setPriority(guideDetails.getPriority());
        existingGuide.setUpdatedAt(LocalDateTime.now());

        return firstAidGuideRepository.save(existingGuide);
    }

    @Override
    public void deleteFirstAidGuide(Long id) {
        FirstAidGuide guide = firstAidGuideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("First aid guide not found with id: " + id));
        firstAidGuideRepository.delete(guide);
    }

    @Override
    public long countFirstAidGuides() {
        return firstAidGuideRepository.count();
    }

    @Override
    public List<String> getAllCategories() {
        return firstAidGuideRepository.findDistinctCategories();
    }

    @Override
    public FirstAidGuide createFirstAidGuide(FirstAidGuide guide) {
        guide.setCreatedAt(LocalDateTime.now());
        guide.setUpdatedAt(LocalDateTime.now());
        return firstAidGuideRepository.save(guide);
    }

    @Override
    public boolean existsById(Long id) {
        return firstAidGuideRepository.existsById(id);
    }
}
