package com.pharmacy.inventory.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class SalesReportResponse {

    private BigDecimal todaySales;
    private BigDecimal thisMonthRevenue;
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;
    private Long totalOrders;
    private Long lowStockItems;

    private List<Map<String, Object>> dailySales;
    private List<Map<String, Object>> monthlySales;
    private List<Map<String, Object>> topSellingMedicines;
    private List<Map<String, Object>> recentTransactions;

    public SalesReportResponse() {}

    public BigDecimal getTodaySales() {
        return todaySales;
    }

    public void setTodaySales(BigDecimal todaySales) {
        this.todaySales = todaySales;
    }

    public BigDecimal getThisMonthRevenue() {
        return thisMonthRevenue;
    }

    public void setThisMonthRevenue(BigDecimal thisMonthRevenue) {
        this.thisMonthRevenue = thisMonthRevenue;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(BigDecimal totalProfit) {
        this.totalProfit = totalProfit;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getLowStockItems() {
        return lowStockItems;
    }

    public void setLowStockItems(Long lowStockItems) {
        this.lowStockItems = lowStockItems;
    }

    public List<Map<String, Object>> getDailySales() {
        return dailySales;
    }

    public void setDailySales(List<Map<String, Object>> dailySales) {
        this.dailySales = dailySales;
    }

    public List<Map<String, Object>> getMonthlySales() {
        return monthlySales;
    }

    public void setMonthlySales(List<Map<String, Object>> monthlySales) {
        this.monthlySales = monthlySales;
    }

    public List<Map<String, Object>> getTopSellingMedicines() {
        return topSellingMedicines;
    }

    public void setTopSellingMedicines(List<Map<String, Object>> topSellingMedicines) {
        this.topSellingMedicines = topSellingMedicines;
    }

    public List<Map<String, Object>> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<Map<String, Object>> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }
}
