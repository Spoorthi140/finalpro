package com.pharmacy.inventory.dto.response;

public class CategoryDistributionResponse {
    private String categoryName;
    private int medicationCount;

    // Constructors
    public CategoryDistributionResponse() {}

    public CategoryDistributionResponse(String categoryName, int medicationCount) {
        this.categoryName = categoryName;
        this.medicationCount = medicationCount;
    }

    // Getters and setters
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getMedicationCount() { return medicationCount; }
    public void setMedicationCount(int medicationCount) { this.medicationCount = medicationCount; }
}