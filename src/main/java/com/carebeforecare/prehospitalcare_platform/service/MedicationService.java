package com.carebeforecare.prehospitalcare_platform.service;

import com.carebeforecare.prehospitalcare_platform.model.entity.Medication;
import java.util.List;

public interface MedicationService {
    List<Medication> getAllMedications();
    Medication getMedicationById(Long id);
    List<Medication> getMedicationsByDrugClass(String drugClass);
    List<Medication> getMedicationsByPrescriptionRequired(boolean prescriptionRequired);
    List<Medication> searchMedications(String query);
    Medication saveMedication(Medication medication);
    Medication updateMedication(Long id, Medication medicationDetails);
    void deleteMedication(Long id);
    long countMedications();
    List<String> getAllDrugClasses();

    // New methods for admin operations
    Medication createMedication(Medication medication);
    boolean existsById(Long id);
}