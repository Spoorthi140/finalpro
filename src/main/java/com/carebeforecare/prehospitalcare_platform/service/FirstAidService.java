package com.carebeforecare.prehospitalcare_platform.service;

import com.carebeforecare.prehospitalcare_platform.model.entity.FirstAidGuide;
import java.util.List;
import java.util.Optional;

public interface FirstAidService {
    List<FirstAidGuide> getAllFirstAidGuides();
    Optional<FirstAidGuide> getFirstAidGuideById(Long id);
    List<FirstAidGuide> getFirstAidGuidesByCategory(String category);
    List<FirstAidGuide> searchFirstAidGuides(String keyword);
    List<FirstAidGuide> getRelatedFirstAidGuides(String category, Long excludeId);
    FirstAidGuide saveFirstAidGuide(FirstAidGuide guide);
    FirstAidGuide updateFirstAidGuide(Long id, FirstAidGuide guideDetails);
    void deleteFirstAidGuide(Long id);
    long countFirstAidGuides();
    List<String> getAllCategories();

    // Make sure these method signatures match exactly
    FirstAidGuide createFirstAidGuide(FirstAidGuide guide);
    boolean existsById(Long id);
}