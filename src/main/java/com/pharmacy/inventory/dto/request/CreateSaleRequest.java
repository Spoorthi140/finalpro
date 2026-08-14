package com.pharmacy.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateSaleRequest {

    @NotNull(message = "Medication ID is required")
    private Long medicationId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    public CreateSaleRequest() {}

    public CreateSaleRequest(Long medicationId, Integer quantity) {
        this.medicationId = medicationId;
        this.quantity = quantity;
    }

    public Long getMedicationId() {
        return medicationId;
    }

    public void setMedicationId(Long medicationId) {
        this.medicationId = medicationId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
