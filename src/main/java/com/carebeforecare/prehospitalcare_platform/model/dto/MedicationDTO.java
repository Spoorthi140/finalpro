package com.carebeforecare.prehospitalcare_platform.model.dto;

import jakarta.validation.constraints.NotBlank;

public class MedicationDTO {
    private Long id;

    @NotBlank(message = "Medication name is required")
    private String name;

    private String genericName;
    private String dosageForms;
    private String commonUses;
    private String sideEffects;
    private String precautions;
    private String usageInstructions;
    private String drugClass;
    private boolean prescriptionRequired;
    private String storageInstructions;

    // Constructors
    public MedicationDTO() {
    }

    public MedicationDTO(Long id, String name, String genericName, String drugClass) {
        this.id = id;
        this.name = name;
        this.genericName = genericName;
        this.drugClass = drugClass;
    }

    // Getters and Setters
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

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getDosageForms() {
        return dosageForms;
    }

    public void setDosageForms(String dosageForms) {
        this.dosageForms = dosageForms;
    }

    public String getCommonUses() {
        return commonUses;
    }

    public void setCommonUses(String commonUses) {
        this.commonUses = commonUses;
    }

    public String getSideEffects() {
        return sideEffects;
    }

    public void setSideEffects(String sideEffects) {
        this.sideEffects = sideEffects;
    }

    public String getPrecautions() {
        return precautions;
    }

    public void setPrecautions(String precautions) {
        this.precautions = precautions;
    }

    public String getUsageInstructions() {
        return usageInstructions;
    }

    public void setUsageInstructions(String usageInstructions) {
        this.usageInstructions = usageInstructions;
    }

    public String getDrugClass() {
        return drugClass;
    }

    public void setDrugClass(String drugClass) {
        this.drugClass = drugClass;
    }

    public boolean isPrescriptionRequired() {
        return prescriptionRequired;
    }

    public void setPrescriptionRequired(boolean prescriptionRequired) {
        this.prescriptionRequired = prescriptionRequired;
    }

    public String getStorageInstructions() {
        return storageInstructions;
    }

    public void setStorageInstructions(String storageInstructions) {
        this.storageInstructions = storageInstructions;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MedicationDTO that = (MedicationDTO) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // toString
    @Override
    public String toString() {
        return "MedicationDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", genericName='" + genericName + '\'' +
                ", drugClass='" + drugClass + '\'' +
                ", prescriptionRequired=" + prescriptionRequired +
                '}';
    }
}