package com.carebeforecare.prehospitalcare_platform.service.impl;

import com.carebeforecare.prehospitalcare_platform.model.entity.Medication;
import com.carebeforecare.prehospitalcare_platform.repository.MedicationRepository;
import com.carebeforecare.prehospitalcare_platform.service.MedicationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepository medicationRepository;

    public MedicationServiceImpl(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    @Override
    public List<Medication> getAllMedications() {
        return medicationRepository.findAll();
    }

    @Override
    public Medication getMedicationById(Long id) {
        return medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));
    }

    @Override
    public List<Medication> getMedicationsByDrugClass(String drugClass) {
        return medicationRepository.findByDrugClass(drugClass);
    }

    @Override
    public List<Medication> getMedicationsByPrescriptionRequired(boolean prescriptionRequired) {
        return medicationRepository.findByPrescriptionRequired(prescriptionRequired);
    }

    @Override
    public List<Medication> searchMedications(String query) {
        return medicationRepository.searchMedications(query);
    }

    @Override
    public Medication saveMedication(Medication medication) {
        return medicationRepository.save(medication);
    }

    @Override
    public Medication updateMedication(Long id, Medication medicationDetails) {
        Medication existingMedication = getMedicationById(id);
        existingMedication.setName(medicationDetails.getName());
        existingMedication.setGenericName(medicationDetails.getGenericName());
        existingMedication.setDrugClass(medicationDetails.getDrugClass());
        existingMedication.setDosageForms(medicationDetails.getDosageForms());
        existingMedication.setPrescriptionRequired(medicationDetails.getPrescriptionRequired());
        existingMedication.setCommonUses(medicationDetails.getCommonUses());
        existingMedication.setUsageInstructions(medicationDetails.getUsageInstructions());
        existingMedication.setSideEffects(medicationDetails.getSideEffects());
        existingMedication.setPrecautions(medicationDetails.getPrecautions());
        existingMedication.setStorageInstructions(medicationDetails.getStorageInstructions());
        existingMedication.setUpdatedAt(java.time.LocalDateTime.now());
        return medicationRepository.save(existingMedication);
    }

    @Override
    public void deleteMedication(Long id) {
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));
        medicationRepository.delete(medication);
    }

    @Override
    public long countMedications() {
        return medicationRepository.count();
    }

    @Override
    public List<String> getAllDrugClasses() {
        return medicationRepository.findDistinctDrugClasses();
    }

    @Override
    public Medication createMedication(Medication medication) {
        return medicationRepository.save(medication);
    }

    @Override
    public boolean existsById(Long id) {
        return medicationRepository.existsById(id);
    }
}
