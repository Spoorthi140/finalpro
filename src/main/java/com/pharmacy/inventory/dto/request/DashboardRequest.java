package com.pharmacy.inventory.dto.request;

import java.time.LocalDate;

public class DashboardRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer daysUntilExpiry;
    private String categoryFilter;

    // Constructors
    public DashboardRequest() {}

    public DashboardRequest(LocalDate startDate, LocalDate endDate, Integer daysUntilExpiry, String categoryFilter) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.daysUntilExpiry = daysUntilExpiry;
        this.categoryFilter = categoryFilter;
    }

    // Getters and setters
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getDaysUntilExpiry() { return daysUntilExpiry; }
    public void setDaysUntilExpiry(Integer daysUntilExpiry) { this.daysUntilExpiry = daysUntilExpiry; }

    public String getCategoryFilter() { return categoryFilter; }
    public void setCategoryFilter(String categoryFilter) { this.categoryFilter = categoryFilter; }
}