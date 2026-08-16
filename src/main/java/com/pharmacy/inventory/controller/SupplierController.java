package com.pharmacy.inventory.controller;

import com.pharmacy.inventory.dto.request.SupplierRequest;
import com.pharmacy.inventory.dto.response.ApiResponse;
import com.pharmacy.inventory.dto.response.SupplierResponse;
import com.pharmacy.inventory.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getAllSuppliers() {
        try {
            List<SupplierResponse> suppliers = supplierService.getAllSuppliers();
            return ResponseEntity.ok(ApiResponse.success("Suppliers retrieved successfully", suppliers));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve suppliers: " + e.getMessage()));
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<SupplierResponse>>> getAllSuppliersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<SupplierResponse> suppliers = supplierService.getAllSuppliers(pageable);
            return ResponseEntity.ok(ApiResponse.success("Suppliers retrieved successfully", suppliers));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve suppliers: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> getSupplierById(@PathVariable Long id) {
        try {
            SupplierResponse supplier = supplierService.getSupplierById(id);
            return ResponseEntity.ok(ApiResponse.success("Supplier retrieved successfully", supplier));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve supplier: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(@Valid @RequestBody SupplierRequest supplierRequest) {
        try {
            SupplierResponse supplier = supplierService.createSupplier(supplierRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Supplier created successfully", supplier));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create supplier: " + e.getMessage()));
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> createSuppliers(@Valid @RequestBody List<SupplierRequest> supplierRequests) {
        try {
            List<SupplierResponse> suppliers = supplierService.createSuppliers(supplierRequests);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Suppliers created successfully", suppliers));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create suppliers: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest supplierRequest) {
        try {
            SupplierResponse supplier = supplierService.updateSupplier(id, supplierRequest);
            return ResponseEntity.ok(ApiResponse.success("Supplier updated successfully", supplier));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update supplier: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable Long id) {
        try {
            supplierService.deleteSupplier(id);
            return ResponseEntity.ok(ApiResponse.success("Supplier deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete supplier: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> searchSuppliers(@RequestParam String query) {
        try {
            List<SupplierResponse> suppliers = supplierService.searchSuppliers(query);
            return ResponseEntity.ok(ApiResponse.success("Search results retrieved", suppliers));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to search suppliers: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getActiveSuppliers() {
        try {
            List<SupplierResponse> suppliers = supplierService.getActiveSuppliers();
            return ResponseEntity.ok(ApiResponse.success("Active suppliers retrieved successfully", suppliers));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve active suppliers: " + e.getMessage()));
        }
    }

    @GetMapping("/status/{active}")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getSuppliersByStatus(@PathVariable Boolean active) {
        try {
            List<SupplierResponse> suppliers = supplierService.getSuppliersByStatus(active);
            return ResponseEntity.ok(ApiResponse.success("Suppliers retrieved by status successfully", suppliers));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve suppliers by status: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<SupplierResponse>> toggleSupplierStatus(@PathVariable Long id) {
        try {
            SupplierResponse supplier = supplierService.toggleSupplierStatus(id);
            return ResponseEntity.ok(ApiResponse.success("Supplier status updated successfully", supplier));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to toggle supplier status: " + e.getMessage()));
        }
    }

    @PostMapping("/bulk-deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateSuppliers(@RequestBody List<Long> supplierIds) {
        try {
            supplierService.deactivateSuppliers(supplierIds);
            return ResponseEntity.ok(ApiResponse.success("Suppliers deactivated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to deactivate suppliers: " + e.getMessage()));
        }
    }
}