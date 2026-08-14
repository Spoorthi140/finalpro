package com.pharmacy.inventory.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MedicationRequest {
    private String name;
    private String description;
    private Integer stockQuantity;
    private BigDecimal unitPrice;
    private LocalDate expiryDate;
    private Integer lowStockThreshold;
    private String manufacturer;
    private String batchNumber;
    private String type;
    private Long categoryId;
    private Long supplierId;

    // Default constructor
    public MedicationRequest() {}

    // Constructor with basic fields
    public MedicationRequest(String name, String description, Integer stockQuantity,
                           BigDecimal unitPrice, LocalDate expiryDate) {
        this.name = name;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.unitPrice = unitPrice;
        this.expiryDate = expiryDate;
    }

    // Constructor with all fields
    public MedicationRequest(String name, String description, Integer stockQuantity,
                           BigDecimal unitPrice, LocalDate expiryDate, Integer lowStockThreshold,
                           String manufacturer, String batchNumber, String type,
                           Long categoryId, Long supplierId) {
        this.name = name;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.unitPrice = unitPrice;
        this.expiryDate = expiryDate;
        this.lowStockThreshold = lowStockThreshold;
        this.manufacturer = manufacturer;
        this.batchNumber = batchNumber;
        this.type = type;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "MedicationRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", stockQuantity=" + stockQuantity +
                ", unitPrice=" + unitPrice +
                ", expiryDate=" + expiryDate +
                ", lowStockThreshold=" + lowStockThreshold +
                ", manufacturer='" + manufacturer + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                ", type='" + type + '\'' +
                ", categoryId=" + categoryId +
                ", supplierId=" + supplierId +
                '}';
    }
}