package com.pharmacy.inventory.service;

import com.pharmacy.inventory.dto.response.*;

import java.util.List;

public interface DashboardService {
    DashboardStatsResponse getDashboardStats();
    List<MedicationResponse> getRecentMedications();
    List<CategoryDistributionResponse> getCategoryDistribution();
    List<StockDistributionResponse> getStockDistribution();
    List<MedicationResponse> getExpiringSoonMedications();
}