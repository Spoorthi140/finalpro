package com.pharmacy.inventory.controller;

import com.pharmacy.inventory.dto.request.CreateSaleRequest;
import com.pharmacy.inventory.dto.response.ApiResponse;
import com.pharmacy.inventory.dto.response.SalesReportResponse;
import com.pharmacy.inventory.entity.Sale;
import com.pharmacy.inventory.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    @Autowired
    private SaleService saleService;

    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<Sale>>> getAllSales() {
        try {
            java.util.List<Sale> sales = saleService.getAllSales();
            return ResponseEntity.ok(ApiResponse.success("Sales retrieved successfully", sales));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve sales: " + e.getMessage()));
        }
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<SalesReportResponse>> getSalesReport() {
        try {
            SalesReportResponse report = saleService.getSalesReport();
            return ResponseEntity.ok(ApiResponse.success("Sales report generated successfully", report));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to generate sales report: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Sale>> createSale(@Valid @RequestBody CreateSaleRequest request) {
        try {
            Sale sale = saleService.createSale(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Sale created successfully", sale));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create sale: " + e.getMessage()));
        }
    }
}
