package com.smarturban.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "location_routing_rules")
public class LocationRoutingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ruleName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_department_id", nullable = false)
    private Department targetDepartment;

    private Double minLatitude;
    private Double maxLatitude;
    private Double minLongitude;
    private Double maxLongitude;

    private boolean enabled = true;

    public LocationRoutingRule() {}

    public LocationRoutingRule(String ruleName, Category category, Department targetDepartment,
                               Double minLatitude, Double maxLatitude, Double minLongitude, Double maxLongitude) {
        this.ruleName = ruleName;
        this.category = category;
        this.targetDepartment = targetDepartment;
        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
        this.enabled = true;
    }

    public boolean matches(Double lat, Double lng, String categoryName) {
        if (!enabled || lat == null || lng == null) {
            return false;
        }
        if (minLatitude != null && lat < minLatitude) return false;
        if (maxLatitude != null && lat > maxLatitude) return false;
        if (minLongitude != null && lng < minLongitude) return false;
        if (maxLongitude != null && lng > maxLongitude) return false;

        if (this.category != null && categoryName != null) {
            return this.category.getName().equalsIgnoreCase(categoryName);
        }
        return true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Department getTargetDepartment() { return targetDepartment; }
    public void setTargetDepartment(Department targetDepartment) { this.targetDepartment = targetDepartment; }

    public Double getMinLatitude() { return minLatitude; }
    public void setMinLatitude(Double minLatitude) { this.minLatitude = minLatitude; }

    public Double getMaxLatitude() { return maxLatitude; }
    public void setMaxLatitude(Double maxLatitude) { this.maxLatitude = maxLatitude; }

    public Double getMinLongitude() { return minLongitude; }
    public void setMinLongitude(Double minLongitude) { this.minLongitude = minLongitude; }

    public Double getMaxLongitude() { return maxLongitude; }
    public void setMaxLongitude(Double maxLongitude) { this.maxLongitude = maxLongitude; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
