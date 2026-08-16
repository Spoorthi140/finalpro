package com.pharmacy.inventory.service;

import com.pharmacy.inventory.dto.request.SupplierRequest;
import com.pharmacy.inventory.dto.response.SupplierResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SupplierService {

    List<SupplierResponse> getAllSuppliers();

    Page<SupplierResponse> getAllSuppliers(Pageable pageable);

    SupplierResponse getSupplierById(Long id);

    SupplierResponse createSupplier(SupplierRequest supplierRequest);

    SupplierResponse updateSupplier(Long id, SupplierRequest supplierRequest);

    void deleteSupplier(Long id);

    List<SupplierResponse> searchSuppliers(String searchTerm);

    List<SupplierResponse> getActiveSuppliers();

    List<SupplierResponse> getSuppliersByStatus(Boolean active);

    SupplierResponse toggleSupplierStatus(Long id);

    List<SupplierResponse> createSuppliers(List<SupplierRequest> supplierRequests);

    void deactivateSuppliers(List<Long> supplierIds);
}