package com.pharmacy.inventory.dto.response;

public class StockDistributionResponse {
    private String categoryName;
    private int totalStock;

    // Constructors
    public StockDistributionResponse() {}

    public StockDistributionResponse(String categoryName, int totalStock) {
        this.categoryName = categoryName;
        this.totalStock = totalStock;
    }

    // Getters and setters
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getTotalStock() { return totalStock; }
    public void setTotalStock(int totalStock) { this.totalStock = totalStock; }
}