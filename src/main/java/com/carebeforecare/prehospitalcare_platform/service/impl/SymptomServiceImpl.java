package com.carebeforecare.prehospitalcare_platform.service.impl;

import com.carebeforecare.prehospitalcare_platform.model.entity.Symptom;
import com.carebeforecare.prehospitalcare_platform.repository.SymptomRepository;
import com.carebeforecare.prehospitalcare_platform.service.SymptomService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SymptomServiceImpl implements SymptomService {

    private final SymptomRepository symptomRepository;

    public SymptomServiceImpl(SymptomRepository symptomRepository) {
        this.symptomRepository = symptomRepository;
    }

    @Override
    public List<Symptom> getAllSymptoms() {
        return symptomRepository.findAll();
    }

    @Override
    public Optional<Symptom> getSymptomById(Long id) {
        return symptomRepository.findById(id);
    }

    @Override
    public List<Symptom> getSymptomsByCategory(String category) {
        return symptomRepository.findByCategory(category);
    }

    @Override
    public List<Symptom> searchSymptoms(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllSymptoms();
        }
        return symptomRepository.searchSymptoms(keyword);
    }

    @Override
    public Symptom saveSymptom(Symptom symptom) {
        return symptomRepository.save(symptom);
    }

    @Override
    public Symptom updateSymptom(Long id, Symptom symptomDetails) {
        Symptom existingSymptom = symptomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Symptom not found with id: " + id));

        existingSymptom.setName(symptomDetails.getName());
        existingSymptom.setCategory(symptomDetails.getCategory());
        existingSymptom.setSeverity(symptomDetails.getSeverity());
        existingSymptom.setDescription(symptomDetails.getDescription());
        existingSymptom.setImmediateActions(symptomDetails.getImmediateActions());
        existingSymptom.setWarningSigns(symptomDetails.getWarningSigns());
        existingSymptom.setWhenToSeekHelp(symptomDetails.getWhenToSeekHelp());
        existingSymptom.setUpdatedAt(java.time.LocalDateTime.now());
        return symptomRepository.save(existingSymptom);
    }

    @Override
    public void deleteSymptom(Long id) {
        Symptom symptom = symptomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Symptom not found with id: " + id));
        symptomRepository.delete(symptom);
    }

    @Override
    public long countSymptoms() {
        return symptomRepository.count();
    }

    @Override
    public List<String> getAllCategories() {
        return symptomRepository.findDistinctCategories();
    }

    @Override
    public Symptom createSymptom(Symptom symptom) {
        return symptomRepository.save(symptom);
    }

    @Override
    public boolean existsById(Long id) {
        return symptomRepository.existsById(id);
    }
}
