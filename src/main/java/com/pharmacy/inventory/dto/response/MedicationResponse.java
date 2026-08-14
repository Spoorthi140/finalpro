package com.pharmacy.inventory.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MedicationResponse {
    private Long id;
    private String name;
    private String description;
    private Integer stockQuantity;
    private BigDecimal unitPrice;
    private LocalDate expiryDate;
    private Integer lowStockThreshold;
    private String manufacturer;
    private String batchNumber;
    private String type;
    private String categoryName;
    private Long categoryId;
    private String supplierName;
    private Long supplierId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String stockStatus;

    // Default constructor
    public MedicationResponse() {}

    // Constructor with basic fields
    public MedicationResponse(Long id, String name, String description, Integer stockQuantity,
                             BigDecimal unitPrice, LocalDate expiryDate, Integer lowStockThreshold,
                             String manufacturer, String batchNumber, String categoryName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.unitPrice = unitPrice;
        this.expiryDate = expiryDate;
        this.lowStockThreshold = lowStockThreshold;
        this.manufacturer = manufacturer;
        this.batchNumber = batchNumber;
        this.categoryName = categoryName;
    }

    // Constructor with all fields
    public MedicationResponse(Long id, String name, String description, Integer stockQuantity,
                             BigDecimal unitPrice, LocalDate expiryDate, Integer lowStockThreshold,
                             String manufacturer, String batchNumber, String type, String categoryName,
                             Long categoryId, String supplierName, Long supplierId,
                             LocalDateTime createdAt, LocalDateTime updatedAt, String stockStatus) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.unitPrice = unitPrice;
        this.expiryDate = expiryDate;
        this.lowStockThreshold = lowStockThreshold;
        this.manufacturer = manufacturer;
        this.batchNumber = batchNumber;
        this.type = type;
        this.categoryName = categoryName;
        this.categoryId = categoryId;
        this.supplierName = supplierName;
        this.supplierId = supplierId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.stockStatus = stockStatus;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "MedicationResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", stockQuantity=" + stockQuantity +
                ", unitPrice=" + unitPrice +
                ", expiryDate=" + expiryDate +
                ", lowStockThreshold=" + lowStockThreshold +
                ", manufacturer='" + manufacturer + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                ", type='" + type + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", categoryId=" + categoryId +
                ", supplierName='" + supplierName + '\'' +
                ", supplierId=" + supplierId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", stockStatus='" + stockStatus + '\'' +
                '}';
    }
}