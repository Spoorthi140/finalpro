package com.pharmacy.inventory.service.impl;

import com.pharmacy.inventory.dto.response.*;
import com.pharmacy.inventory.entity.Medication;
import com.pharmacy.inventory.repository.DashboardRepository;
import com.pharmacy.inventory.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;

    @Autowired
    public DashboardServiceImpl(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        try {
            Long totalMedications = dashboardRepository.countTotalMedications();
            Long lowStockItems = dashboardRepository.countLowStockItems();
            Long outOfStockItems = dashboardRepository.countOutOfStockItems();
            Long totalCategories = dashboardRepository.countTotalCategories();
            BigDecimal totalInventoryValue = dashboardRepository.calculateTotalInventoryValue();

            // Calculate medications expiring in next 30 days
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(30);
            Long medicationsExpiringSoon = dashboardRepository.countMedicationsExpiringSoon(startDate, endDate);

            return new DashboardStatsResponse(
                totalMedications != null ? totalMedications : 0L,
                lowStockItems != null ? lowStockItems : 0L,
                outOfStockItems != null ? outOfStockItems : 0L,
                totalCategories != null ? totalCategories : 0L,
                totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO,
                medicationsExpiringSoon != null ? medicationsExpiringSoon : 0L
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve dashboard statistics: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationResponse> getRecentMedications() {
        try {
            List<Medication> recentMedications = dashboardRepository.findRecentMedications();
            return recentMedications.stream()
                    .map(this::convertToMedicationResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve recent medications: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDistributionResponse> getCategoryDistribution() {
        try {
            List<Object[]> distributionData = dashboardRepository.findCategoryDistribution();
            return distributionData.stream()
                    .map(data -> {
                        String categoryName = (String) data[0];
                        Long count = (Long) data[1];
                        return new CategoryDistributionResponse(
                            categoryName != null ? categoryName : "Uncategorized",
                            count != null ? count.intValue() : 0
                        );
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve category distribution: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDistributionResponse> getStockDistribution() {
        try {
            List<Object[]> stockData = dashboardRepository.findStockDistribution();
            return stockData.stream()
                    .map(data -> {
                        String categoryName = (String) data[0];
                        Long totalStock = data[1] != null ? (Long) data[1] : 0L;
                        return new StockDistributionResponse(
                            categoryName != null ? categoryName : "Uncategorized",
                            totalStock.intValue()
                        );
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve stock distribution: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationResponse> getExpiringSoonMedications() {
        try {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(30);
            List<Medication> expiringMedications = dashboardRepository.findExpiringSoonMedications(startDate, endDate);

            return expiringMedications.stream()
                    .map(this::convertToMedicationResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve expiring soon medications: " + e.getMessage(), e);
        }
    }

    private MedicationResponse convertToMedicationResponse(Medication medication) {
        MedicationResponse response = new MedicationResponse();
        response.setId(medication.getId());
        response.setName(medication.getName());
        response.setDescription(medication.getDescription());
        response.setStockQuantity(medication.getQuantity()); // Using getQuantity() instead of getStockQuantity()
        response.setUnitPrice(medication.getPrice()); // Using getPrice() instead of getUnitPrice()
        response.setExpiryDate(medication.getExpirationDate()); // Using getExpirationDate() instead of getExpiryDate()
        response.setLowStockThreshold(medication.getLowStockThreshold());
        response.setManufacturer(medication.getManufacturer());
        response.setBatchNumber(medication.getBatchNumber());
        response.setCreatedAt(medication.getCreatedAt());
        response.setUpdatedAt(medication.getUpdatedAt());

        if (medication.getCategory() != null) {
            response.setCategoryName(medication.getCategory().getName());
            response.setCategoryId(medication.getCategory().getId());
        }

        if (medication.getSupplier() != null) {
            response.setSupplierName(medication.getSupplier().getName());
            response.setSupplierId(medication.getSupplier().getId());
        }

        // Calculate stock status
        String stockStatus = calculateStockStatus(medication.getQuantity(), medication.getLowStockThreshold());
        response.setStockStatus(stockStatus);

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