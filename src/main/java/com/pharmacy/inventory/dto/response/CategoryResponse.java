package com.pharmacy.inventory.dto.response;

public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private int medicationCount;

    // Constructors
    public CategoryResponse() {}

    public CategoryResponse(Long id, String name, String description, int medicationCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.medicationCount = medicationCount;
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

    public int getMedicationCount() {
        return medicationCount;
    }

    public void setMedicationCount(int medicationCount) {
        this.medicationCount = medicationCount;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "CategoryResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", medicationCount=" + medicationCount +
                '}';
    }
}