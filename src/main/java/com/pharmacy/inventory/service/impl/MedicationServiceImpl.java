package com.pharmacy.inventory.service.impl;

import com.pharmacy.inventory.dto.request.MedicationRequest;
import com.pharmacy.inventory.dto.response.MedicationResponse;
import com.pharmacy.inventory.entity.Medication;
import com.pharmacy.inventory.entity.Category;
import com.pharmacy.inventory.entity.Supplier;
import com.pharmacy.inventory.repository.MedicationRepository;
import com.pharmacy.inventory.repository.CategoryRepository;
import com.pharmacy.inventory.repository.SupplierRepository;
import com.pharmacy.inventory.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepository medicationRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    @Autowired
    public MedicationServiceImpl(MedicationRepository medicationRepository,
                               CategoryRepository categoryRepository,
                               SupplierRepository supplierRepository) {
        this.medicationRepository = medicationRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationResponse> getAllMedications() {
        try {
            List<Medication> medications = medicationRepository.findByDeletedFalse();
            return medications.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve medications: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MedicationResponse getMedicationById(Long id) {
        try {
            Medication medication = medicationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));

            if (medication.getDeleted()) {
                throw new RuntimeException("Medication with id " + id + " has been deleted");
            }

            return convertToResponse(medication);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve medication with id " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public MedicationResponse createMedication(MedicationRequest medicationRequest) {
        try {
            // Validate input
            validateMedicationRequest(medicationRequest);

            // Check if medication with same name already exists
            if (medicationRepository.findByName(medicationRequest.getName()).isPresent()) {
                throw new RuntimeException("Medication with name '" + medicationRequest.getName() + "' already exists");
            }

            // Create new medication
            Medication medication = new Medication();
            mapRequestToEntity(medicationRequest, medication);

            // Set timestamps
            medication.setCreatedAt(LocalDateTime.now());
            medication.setUpdatedAt(LocalDateTime.now());
            medication.setDeleted(false);

            // Handle category association
            if (medicationRequest.getCategoryId() != null) {
                Category category = categoryRepository.findById(medicationRequest.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found with id: " + medicationRequest.getCategoryId()));
                medication.setCategory(category);
            }

            // Handle supplier association
            if (medicationRequest.getSupplierId() != null) {
                Supplier supplier = supplierRepository.findById(medicationRequest.getSupplierId())
                        .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + medicationRequest.getSupplierId()));
                medication.setSupplier(supplier);
            }

            Medication savedMedication = medicationRepository.save(medication);
            return convertToResponse(savedMedication);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create medication: " + e.getMessage(), e);
        }
    }

    @Override
    public MedicationResponse updateMedication(Long id, MedicationRequest medicationRequest) {
        try {
            // Validate input
            validateMedicationRequest(medicationRequest);

            Medication existingMedication = medicationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));

            if (existingMedication.getDeleted()) {
                throw new RuntimeException("Cannot update deleted medication with id: " + id);
            }

            // Check if another medication with the same name exists (excluding current medication)
            medicationRepository.findByName(medicationRequest.getName())
                    .ifPresent(medication -> {
                        if (!medication.getId().equals(id)) {
                            throw new RuntimeException("Medication with name '" + medicationRequest.getName() + "' already exists");
                        }
                    });

            // Update medication
            mapRequestToEntity(medicationRequest, existingMedication);
            existingMedication.setUpdatedAt(LocalDateTime.now());

            // Handle category association
            if (medicationRequest.getCategoryId() != null) {
                Category category = categoryRepository.findById(medicationRequest.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found with id: " + medicationRequest.getCategoryId()));
                existingMedication.setCategory(category);
            } else {
                existingMedication.setCategory(null);
            }

            // Handle supplier association
            if (medicationRequest.getSupplierId() != null) {
                Supplier supplier = supplierRepository.findById(medicationRequest.getSupplierId())
                        .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + medicationRequest.getSupplierId()));
                existingMedication.setSupplier(supplier);
            } else {
                existingMedication.setSupplier(null);
            }

            Medication updatedMedication = medicationRepository.save(existingMedication);
            return convertToResponse(updatedMedication);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update medication: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteMedication(Long id) {
        try {
            Medication medication = medicationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));

            // Soft delete implementation
            medication.setDeleted(true);
            medication.setUpdatedAt(LocalDateTime.now());
            medicationRepository.save(medication);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete medication: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationResponse> getMedicationsByType(String medicationType) {
        try {
            // Use findByTypeAndDeletedFalse to only get active medications
            List<Medication> medications = medicationRepository.findByTypeAndDeletedFalse(medicationType);

            return medications.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve medications by type: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationResponse> searchMedications(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getAllMedications();
            }

            String trimmedSearchTerm = searchTerm.trim();
            // Use findByNameContainingIgnoreCaseAndDeletedFalse to only get active medications
            List<Medication> medications = medicationRepository.findByNameContainingIgnoreCaseAndDeletedFalse(trimmedSearchTerm);

            return medications.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search medications: " + e.getMessage(), e);
        }
    }

    // Helper methods
    private void validateMedicationRequest(MedicationRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Medication name cannot be null or empty");
        }
        if (request.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        if (request.getUnitPrice() == null || request.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must be a positive value");
        }
        if (request.getExpiryDate() != null && request.getExpiryDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Expiry date cannot be in the past");
        }

        // Name length validation
        if (request.getName().trim().length() < 2 || request.getName().trim().length() > 255) {
            throw new IllegalArgumentException("Medication name must be between 2 and 255 characters");
        }
    }

    private void mapRequestToEntity(MedicationRequest request, Medication medication) {
        medication.setName(request.getName().trim());
        medication.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        medication.setQuantity(request.getStockQuantity());
        medication.setPrice(request.getUnitPrice());
        medication.setExpirationDate(request.getExpiryDate());
        medication.setLowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10);
        medication.setManufacturer(request.getManufacturer() != null ? request.getManufacturer().trim() : null);
        medication.setBatchNumber(request.getBatchNumber() != null ? request.getBatchNumber().trim() : null);
        medication.setType(request.getType() != null ? request.getType().trim() : null);
    }

    private MedicationResponse convertToResponse(Medication medication) {
        MedicationResponse response = new MedicationResponse();
        response.setId(medication.getId());
        response.setName(medication.getName());
        response.setDescription(medication.getDescription());
        response.setStockQuantity(medication.getQuantity());
        response.setUnitPrice(medication.getPrice());
        response.setExpiryDate(medication.getExpirationDate());
        response.setLowStockThreshold(medication.getLowStockThreshold());
        response.setManufacturer(medication.getManufacturer());
        response.setBatchNumber(medication.getBatchNumber());
        response.setType(medication.getType());
        response.setCreatedAt(medication.getCreatedAt());
        response.setUpdatedAt(medication.getUpdatedAt());

        // Set category information if available
        if (medication.getCategory() != null) {
            response.setCategoryName(medication.getCategory().getName());
            response.setCategoryId(medication.getCategory().getId());
        }

        // Set supplier information if available
        if (medication.getSupplier() != null) {
            response.setSupplierName(medication.getSupplier().getName());
            response.setSupplierId(medication.getSupplier().getId());
        }

        // Calculate stock status
        response.setStockStatus(calculateStockStatus(medication.getQuantity(), medication.getLowStockThreshold()));

        return response;
    }

    private String calculateStockStatus(int stockQuantity, int lowStockThreshold) {
        if (stockQuantity <= 0) {
            return "OUT_OF_STOCK";
        } else if (stockQuantity < lowStockThreshold) {
            return "LOW_STOCK";
        } else {
            return "IN_STOCK";
        }
    }
}