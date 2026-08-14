package com.pharmacy.inventory.controller;

import com.pharmacy.inventory.dto.request.MedicationRequest;
import com.pharmacy.inventory.dto.response.ApiResponse;
import com.pharmacy.inventory.dto.response.MedicationResponse;
import com.pharmacy.inventory.service.MedicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
@CrossOrigin(origins = "*")
public class MedicationController {

    @Autowired
    private MedicationService medicationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicationResponse>>> getAllMedications() {
        try {
            List<MedicationResponse> medications = medicationService.getAllMedications();
            return ResponseEntity.ok(ApiResponse.success("Medications retrieved successfully", medications));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve medications: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicationResponse>> getMedicationById(@PathVariable Long id) {
        try {
            MedicationResponse medication = medicationService.getMedicationById(id);
            return ResponseEntity.ok(ApiResponse.success("Medication retrieved successfully", medication));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve medication: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MedicationResponse>> createMedication(@Valid @RequestBody MedicationRequest medicationRequest) {
        try {
            MedicationResponse medication = medicationService.createMedication(medicationRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Medication created successfully", medication));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create medication: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicationResponse>> updateMedication(
            @PathVariable Long id,
            @Valid @RequestBody MedicationRequest medicationRequest) {
        try {
            MedicationResponse medication = medicationService.updateMedication(id, medicationRequest);
            return ResponseEntity.ok(ApiResponse.success("Medication updated successfully", medication));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update medication: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMedication(@PathVariable Long id) {
        try {
            medicationService.deleteMedication(id);
            return ResponseEntity.ok(ApiResponse.success("Medication deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete medication: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MedicationResponse>>> searchMedications(@RequestParam String query) {
        try {
            List<MedicationResponse> medications = medicationService.searchMedications(query);
            return ResponseEntity.ok(ApiResponse.success("Medications search completed", medications));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to search medications: " + e.getMessage()));
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<MedicationResponse>>> getMedicationsByType(@PathVariable String type) {
        try {
            List<MedicationResponse> medications = medicationService.getMedicationsByType(type);
            return ResponseEntity.ok(ApiResponse.success("Medications retrieved by type", medications));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve medications by type: " + e.getMessage()));
        }
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<ApiResponse<List<MedicationResponse>>> getExpiringMedications(@RequestParam(defaultValue = "30") int days) {
        try {
            List<MedicationResponse> medications = medicationService.getExpiringMedications(days);
            return ResponseEntity.ok(ApiResponse.success("Expiring medications retrieved successfully", medications));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve expiring medications: " + e.getMessage()));
        }
    }
}