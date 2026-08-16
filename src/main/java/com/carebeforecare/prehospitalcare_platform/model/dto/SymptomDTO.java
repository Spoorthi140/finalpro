package com.carebeforecare.prehospitalcare_platform.model.dto;

import jakarta.validation.constraints.NotBlank;

public class SymptomDTO {
    private Long id;

    @NotBlank(message = "Symptom name is required")
    private String name;

    private String description;
    private String severity;
    private String category;
    private String immediateActions;
    private String warningSigns;
    private String whenToSeekHelp;

    // Constructors
    public SymptomDTO() {
    }

    public SymptomDTO(Long id, String name, String description, String severity, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.severity = severity;
        this.category = category;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImmediateActions() {
        return immediateActions;
    }

    public void setImmediateActions(String immediateActions) {
        this.immediateActions = immediateActions;
    }

    public String getWarningSigns() {
        return warningSigns;
    }

    public void setWarningSigns(String warningSigns) {
        this.warningSigns = warningSigns;
    }

    public String getWhenToSeekHelp() {
        return whenToSeekHelp;
    }

    public void setWhenToSeekHelp(String whenToSeekHelp) {
        this.whenToSeekHelp = whenToSeekHelp;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SymptomDTO that = (SymptomDTO) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // toString
    @Override
    public String toString() {
        return "SymptomDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", severity='" + severity + '\'' +
                ", category='" + category + '\'' +
                ", immediateActions='" + immediateActions + '\'' +
                ", warningSigns='" + warningSigns + '\'' +
                ", whenToSeekHelp='" + whenToSeekHelp + '\'' +
                '}';
    }
}