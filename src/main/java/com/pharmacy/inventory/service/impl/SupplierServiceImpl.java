package com.pharmacy.inventory.service.impl;

import com.pharmacy.inventory.dto.request.SupplierRequest;
import com.pharmacy.inventory.dto.response.SupplierResponse;
import com.pharmacy.inventory.entity.Supplier;
import com.pharmacy.inventory.exception.SupplierAlreadyExistsException;
import com.pharmacy.inventory.exception.SupplierNotFoundException;
import com.pharmacy.inventory.repository.SupplierRepository;
import com.pharmacy.inventory.service.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierServiceImpl.class);
    private final SupplierRepository supplierRepository;

    @Autowired
    public SupplierServiceImpl(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        try {
            logger.info("Retrieving all suppliers");
            List<Supplier> suppliers = supplierRepository.findAll();
            List<SupplierResponse> responses = suppliers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            logger.info("Successfully retrieved {} suppliers", responses.size());
            return responses;
        } catch (Exception e) {
            logger.error("Failed to retrieve suppliers", e);
            throw new RuntimeException("Failed to retrieve suppliers: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierResponse> getAllSuppliers(Pageable pageable) {
        try {
            logger.info("Retrieving suppliers with pagination - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
            Page<Supplier> suppliersPage = supplierRepository.findAll(pageable);
            Page<SupplierResponse> responsePage = suppliersPage.map(this::convertToResponse);
            logger.info("Successfully retrieved {} suppliers on page {}", responsePage.getNumberOfElements(), pageable.getPageNumber());
            return responsePage;
        } catch (Exception e) {
            logger.error("Failed to retrieve suppliers with pagination", e);
            throw new RuntimeException("Failed to retrieve suppliers: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        try {
            logger.info("Retrieving supplier with id: {}", id);
            Supplier supplier = supplierRepository.findById(id)
                    .orElseThrow(() -> new SupplierNotFoundException(id));
            SupplierResponse response = convertToResponse(supplier);
            logger.info("Successfully retrieved supplier with id: {}", id);
            return response;
        } catch (SupplierNotFoundException e) {
            logger.warn("Supplier not found with id: {}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to retrieve supplier with id: {}", id, e);
            throw new RuntimeException("Failed to retrieve supplier with id " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public SupplierResponse createSupplier(SupplierRequest supplierRequest) {
        try {
            logger.info("Creating new supplier: {}", supplierRequest.getName());

            // Validate input
            validateSupplierRequest(supplierRequest);

            // Check uniqueness constraints
            validateUniquenessConstraints(supplierRequest, null);

            // Create new supplier
            Supplier supplier = new Supplier();
            mapRequestToEntity(supplierRequest, supplier);

            // Set default values
            supplier.setActive(true);
            supplier.setCreatedAt(LocalDateTime.now());
            supplier.setUpdatedAt(LocalDateTime.now());

            Supplier savedSupplier = supplierRepository.save(supplier);
            SupplierResponse response = convertToResponse(savedSupplier);

            logger.info("Successfully created supplier with id: {}", savedSupplier.getId());
            return response;
        } catch (SupplierAlreadyExistsException e) {
            logger.warn("Supplier creation failed - already exists: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.warn("Supplier creation failed - validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create supplier: {}", supplierRequest.getName(), e);
            throw new RuntimeException("Failed to create supplier: " + e.getMessage(), e);
        }
    }

    @Override
    public SupplierResponse updateSupplier(Long id, SupplierRequest supplierRequest) {
        try {
            logger.info("Updating supplier with id: {}", id);

            // Validate input
            validateSupplierRequest(supplierRequest);

            Supplier existingSupplier = supplierRepository.findById(id)
                    .orElseThrow(() -> new SupplierNotFoundException(id));

            // Check uniqueness constraints (excluding current supplier)
            validateUniquenessConstraints(supplierRequest, id);

            // Update supplier
            mapRequestToEntity(supplierRequest, existingSupplier);
            existingSupplier.setUpdatedAt(LocalDateTime.now());

            Supplier updatedSupplier = supplierRepository.save(existingSupplier);
            SupplierResponse response = convertToResponse(updatedSupplier);

            logger.info("Successfully updated supplier with id: {}", id);
            return response;
        } catch (SupplierNotFoundException e) {
            logger.warn("Supplier update failed - not found with id: {}", id);
            throw e;
        } catch (SupplierAlreadyExistsException e) {
            logger.warn("Supplier update failed - uniqueness constraint: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.warn("Supplier update failed - validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update supplier with id: {}", id, e);
            throw new RuntimeException("Failed to update supplier: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteSupplier(Long id) {
        try {
            logger.info("Deleting supplier with id: {}", id);
            Supplier supplier = supplierRepository.findById(id)
                    .orElseThrow(() -> new SupplierNotFoundException(id));

            // Remove medication count check since we removed the medications collection
            // You can add this back later when you implement the proper relationship

            supplierRepository.delete(supplier);
            logger.info("Successfully deleted supplier with id: {}", id);
        } catch (SupplierNotFoundException e) {
            logger.warn("Supplier deletion failed - not found with id: {}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete supplier with id: {}", id, e);
            throw new RuntimeException("Failed to delete supplier: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> searchSuppliers(String searchTerm) {
        try {
            logger.info("Searching suppliers with term: {}", searchTerm);
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getAllSuppliers();
            }

            String trimmedSearchTerm = searchTerm.trim();
            List<Supplier> suppliers = supplierRepository.searchSuppliers(trimmedSearchTerm);
            List<SupplierResponse> responses = suppliers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            logger.info("Search completed. Found {} suppliers for term: {}", responses.size(), trimmedSearchTerm);
            return responses;
        } catch (Exception e) {
            logger.error("Failed to search suppliers with term: {}", searchTerm, e);
            throw new RuntimeException("Failed to search suppliers: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getActiveSuppliers() {
        try {
            logger.info("Retrieving active suppliers");
            List<Supplier> activeSuppliers = supplierRepository.findByActiveTrue();
            List<SupplierResponse> responses = activeSuppliers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            logger.info("Successfully retrieved {} active suppliers", responses.size());
            return responses;
        } catch (Exception e) {
            logger.error("Failed to retrieve active suppliers", e);
            throw new RuntimeException("Failed to retrieve active suppliers: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getSuppliersByStatus(Boolean active) {
        try {
            logger.info("Retrieving suppliers with status: {}", active);
            List<Supplier> suppliers = supplierRepository.findByActive(active);
            List<SupplierResponse> responses = suppliers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            logger.info("Successfully retrieved {} suppliers with status: {}", responses.size(), active);
            return responses;
        } catch (Exception e) {
            logger.error("Failed to retrieve suppliers with status: {}", active, e);
            throw new RuntimeException("Failed to retrieve suppliers by status: " + e.getMessage(), e);
        }
    }

    @Override
    public SupplierResponse toggleSupplierStatus(Long id) {
        try {
            logger.info("Toggling status for supplier with id: {}", id);
            Supplier supplier = supplierRepository.findById(id)
                    .orElseThrow(() -> new SupplierNotFoundException(id));

            boolean newStatus = !supplier.getActive();
            supplier.setActive(newStatus);
            supplier.setUpdatedAt(LocalDateTime.now());

            Supplier updatedSupplier = supplierRepository.save(supplier);
            SupplierResponse response = convertToResponse(updatedSupplier);

            logger.info("Successfully toggled supplier status to: {} for id: {}", newStatus, id);
            return response;
        } catch (SupplierNotFoundException e) {
            logger.warn("Supplier status toggle failed - not found with id: {}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to toggle supplier status for id: {}", id, e);
            throw new RuntimeException("Failed to toggle supplier status: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SupplierResponse> createSuppliers(List<SupplierRequest> supplierRequests) {
        try {
            logger.info("Creating {} suppliers in bulk", supplierRequests.size());
            List<SupplierResponse> responses = supplierRequests.stream()
                    .map(this::createSupplier)
                    .collect(Collectors.toList());

            logger.info("Successfully created {} suppliers in bulk", responses.size());
            return responses;
        } catch (Exception e) {
            logger.error("Failed to create suppliers in bulk", e);
            throw new RuntimeException("Failed to create suppliers in bulk: " + e.getMessage(), e);
        }
    }

    @Override
    public void deactivateSuppliers(List<Long> supplierIds) {
        try {
            logger.info("Deactivating {} suppliers", supplierIds.size());
            supplierIds.forEach(id -> {
                try {
                    Supplier supplier = supplierRepository.findById(id)
                            .orElseThrow(() -> new SupplierNotFoundException(id));
                    supplier.setActive(false);
                    supplier.setUpdatedAt(LocalDateTime.now());
                    supplierRepository.save(supplier);
                    logger.debug("Deactivated supplier with id: {}", id);
                } catch (SupplierNotFoundException e) {
                    logger.warn("Supplier not found during bulk deactivation: {}", id);
                    // Continue with other suppliers even if one is not found
                }
            });
            logger.info("Successfully deactivated {} suppliers", supplierIds.size());
        } catch (Exception e) {
            logger.error("Failed to deactivate suppliers", e);
            throw new RuntimeException("Failed to deactivate suppliers: " + e.getMessage(), e);
        }
    }

    // Helper methods
    private void validateSupplierRequest(SupplierRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name cannot be null or empty");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier email cannot be null or empty");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier phone cannot be null or empty");
        }

        // Enhanced email validation
        String email = request.getEmail().trim();
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format. Please provide a valid email address");
        }

        // Enhanced phone validation (allows international formats)
        String phone = request.getPhone().trim();
        if (!phone.matches("^[+]?[0-9\\s\\-\\(\\)]{10,15}$")) {
            throw new IllegalArgumentException("Invalid phone number format. Please provide a valid phone number (10-15 digits)");
        }

        // Name length validation
        String name = request.getName().trim();
        if (name.length() < 2 || name.length() > 100) {
            throw new IllegalArgumentException("Supplier name must be between 2 and 100 characters");
        }

        // Address length validation (if provided)
        if (request.getAddress() != null && request.getAddress().trim().length() > 500) {
            throw new IllegalArgumentException("Supplier address must not exceed 500 characters");
        }

        // Contact person length validation (if provided)
        if (request.getContactPerson() != null && request.getContactPerson().trim().length() > 100) {
            throw new IllegalArgumentException("Contact person name must not exceed 100 characters");
        }
    }

    private void validateUniquenessConstraints(SupplierRequest request, Long existingSupplierId) {
        String name = request.getName().trim();
        String email = request.getEmail().trim();
        String phone = request.getPhone().trim();

        // Check name uniqueness
        if (supplierRepository.existsByName(name)) {
            supplierRepository.findByName(name)
                    .ifPresent(existingSupplier -> {
                        if (existingSupplierId == null || !existingSupplier.getId().equals(existingSupplierId)) {
                            throw new SupplierAlreadyExistsException("name", name, existingSupplier.getId());
                        }
                    });
        }

        // Check email uniqueness
        if (supplierRepository.existsByEmail(email)) {
            supplierRepository.findByEmail(email)
                    .ifPresent(existingSupplier -> {
                        if (existingSupplierId == null || !existingSupplier.getId().equals(existingSupplierId)) {
                            throw new SupplierAlreadyExistsException("email", email, existingSupplier.getId());
                        }
                    });
        }

        // Check phone uniqueness
        if (supplierRepository.existsByPhone(phone)) {
            supplierRepository.findByPhone(phone)
                    .ifPresent(existingSupplier -> {
                        if (existingSupplierId == null || !existingSupplier.getId().equals(existingSupplierId)) {
                            throw new SupplierAlreadyExistsException("phone", phone, existingSupplier.getId());
                        }
                    });
        }
    }

    private void mapRequestToEntity(SupplierRequest request, Supplier supplier) {
        supplier.setName(request.getName().trim());
        supplier.setEmail(request.getEmail().trim());
        supplier.setPhone(request.getPhone().trim());
        supplier.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        supplier.setContactPerson(request.getContactPerson() != null ? request.getContactPerson().trim() : null);
    }

    private SupplierResponse convertToResponse(Supplier supplier) {
        SupplierResponse response = new SupplierResponse();
        response.setId(supplier.getId());
        response.setName(supplier.getName());
        response.setEmail(supplier.getEmail());
        response.setPhone(supplier.getPhone());
        response.setAddress(supplier.getAddress());
        response.setContactPerson(supplier.getContactPerson());
        response.setActive(supplier.getActive());
        response.setCreatedAt(supplier.getCreatedAt());
        response.setUpdatedAt(supplier.getUpdatedAt());

        // Remove medication count since we removed the medications collection
        response.setMedicationCount(0);

        return response;
    }
}