package com.pharmacy.inventory.controller;

import com.pharmacy.inventory.dto.response.*;
import com.pharmacy.inventory.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        try {
            DashboardStatsResponse stats = dashboardService.getDashboardStats();
            return ResponseEntity.ok(
                ApiResponse.success("Dashboard statistics retrieved successfully", stats)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Failed to retrieve dashboard statistics: " + e.getMessage())
            );
        }
    }

    @GetMapping("/recent-medications")
    public ResponseEntity<ApiResponse<List<MedicationResponse>>> getRecentMedications() {
        try {
            List<MedicationResponse> medications = dashboardService.getRecentMedications();
            return ResponseEntity.ok(
                ApiResponse.success("Recent medications retrieved successfully", medications)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Failed to retrieve recent medications: " + e.getMessage())
            );
        }
    }

    @GetMapping("/category-distribution")
    public ResponseEntity<ApiResponse<List<CategoryDistributionResponse>>> getCategoryDistribution() {
        try {
            List<CategoryDistributionResponse> distribution = dashboardService.getCategoryDistribution();
            return ResponseEntity.ok(
                ApiResponse.success("Category distribution retrieved successfully", distribution)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Failed to retrieve category distribution: " + e.getMessage())
            );
        }
    }

    @GetMapping("/stock-distribution")
    public ResponseEntity<ApiResponse<List<StockDistributionResponse>>> getStockDistribution() {
        try {
            List<StockDistributionResponse> distribution = dashboardService.getStockDistribution();
            return ResponseEntity.ok(
                ApiResponse.success("Stock distribution retrieved successfully", distribution)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Failed to retrieve stock distribution: " + e.getMessage())
            );
        }
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<ApiResponse<List<MedicationResponse>>> getExpiringSoon() {
        try {
            List<MedicationResponse> medications = dashboardService.getExpiringSoonMedications();
            return ResponseEntity.ok(
                ApiResponse.success("Expiring medications retrieved successfully", medications)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Failed to retrieve expiring medications: " + e.getMessage())
            );
        }
    }
}