package com.pharmacy.inventory.service;

import com.pharmacy.inventory.dto.request.MedicationRequest;
import com.pharmacy.inventory.dto.response.MedicationResponse;

import java.util.List;

public interface MedicationService {

    List<MedicationResponse> getAllMedications();

    MedicationResponse getMedicationById(Long id);

    MedicationResponse createMedication(MedicationRequest medicationRequest);

    MedicationResponse updateMedication(Long id, MedicationRequest medicationRequest);

    void deleteMedication(Long id);

    List<MedicationResponse> searchMedications(String searchTerm);

    List<MedicationResponse> getMedicationsByType(String medicationType);
}