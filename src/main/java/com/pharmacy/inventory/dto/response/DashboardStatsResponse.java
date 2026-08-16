package com.pharmacy.inventory.dto.response;

import java.math.BigDecimal;

public class DashboardStatsResponse {
    private Long totalMedications;
    private Long lowStockItems;
    private Long outOfStockItems;
    private Long totalCategories;
    private BigDecimal totalInventoryValue;
    private Long medicationsExpiringSoon;

    // Constructors
    public DashboardStatsResponse() {}

    public DashboardStatsResponse(Long totalMedications, Long lowStockItems,
                                 Long outOfStockItems, Long totalCategories,
                                 BigDecimal totalInventoryValue, Long medicationsExpiringSoon) {
        this.totalMedications = totalMedications;
        this.lowStockItems = lowStockItems;
        this.outOfStockItems = outOfStockItems;
        this.totalCategories = totalCategories;
        this.totalInventoryValue = totalInventoryValue;
        this.medicationsExpiringSoon = medicationsExpiringSoon;
    }

    // Getters and setters
    public Long getTotalMedications() { return totalMedications; }
    public void setTotalMedications(Long totalMedications) { this.totalMedications = totalMedications; }

    public Long getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(Long lowStockItems) { this.lowStockItems = lowStockItems; }

    public Long getOutOfStockItems() { return outOfStockItems; }
    public void setOutOfStockItems(Long outOfStockItems) { this.outOfStockItems = outOfStockItems; }

    public Long getTotalCategories() { return totalCategories; }
    public void setTotalCategories(Long totalCategories) { this.totalCategories = totalCategories; }

    public BigDecimal getTotalInventoryValue() { return totalInventoryValue; }
    public void setTotalInventoryValue(BigDecimal totalInventoryValue) { this.totalInventoryValue = totalInventoryValue; }

    public Long getMedicationsExpiringSoon() { return medicationsExpiringSoon; }
    public void setMedicationsExpiringSoon(Long medicationsExpiringSoon) { this.medicationsExpiringSoon = medicationsExpiringSoon; }
}