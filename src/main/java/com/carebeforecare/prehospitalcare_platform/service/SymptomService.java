package com.carebeforecare.prehospitalcare_platform.service;

import com.carebeforecare.prehospitalcare_platform.model.entity.Symptom;
import java.util.List;
import java.util.Optional;

public interface SymptomService {
    List<Symptom> getAllSymptoms();
    Optional<Symptom> getSymptomById(Long id);
    List<Symptom> getSymptomsByCategory(String category);
    List<Symptom> searchSymptoms(String keyword);
    Symptom saveSymptom(Symptom symptom);
    Symptom updateSymptom(Long id, Symptom symptomDetails);
    void deleteSymptom(Long id);
    long countSymptoms();
    List<String> getAllCategories();

    // New methods for admin operations
    Symptom createSymptom(Symptom symptom);
    boolean existsById(Long id);
}