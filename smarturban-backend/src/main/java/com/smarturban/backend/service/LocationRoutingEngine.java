package com.smarturban.backend.service;

import com.smarturban.backend.entity.Department;
import com.smarturban.backend.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LocationRoutingEngine {

    private static final Logger logger = LoggerFactory.getLogger(LocationRoutingEngine.class);

    @Autowired
    private DepartmentRepository departmentRepository;

    public static class RoutingResult {
        private final Department department;
        private final String routingMethod; // "LOCATION_BASED", "CATEGORY_BASED", or "MANUAL_OVERRIDE"
        private final String matchedZoneName;

        public RoutingResult(Department department, String routingMethod, String matchedZoneName) {
            this.department = department;
            this.routingMethod = routingMethod;
            this.matchedZoneName = matchedZoneName;
        }

        public Department getDepartment() { return department; }
        public String getRoutingMethod() { return routingMethod; }
        public String getMatchedZoneName() { return matchedZoneName; }
    }

    // Dynamic Geographical Sector Rule Structure
    public static class GeoCoverageRule {
        private final String zoneName;
        private final double minLat;
        private final double maxLat;
        private final double minLng;
        private final double maxLng;
        private final String categoryName;
        private final String targetDepartmentName;

        public GeoCoverageRule(String zoneName, double minLat, double maxLat, double minLng, double maxLng, String categoryName, String targetDepartmentName) {
            this.zoneName = zoneName;
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLng = minLng;
            this.maxLng = maxLng;
            this.categoryName = categoryName;
            this.targetDepartmentName = targetDepartmentName;
        }

        public boolean matches(double lat, double lng, String category) {
            boolean inBounds = lat >= minLat && lat <= maxLat && lng >= minLng && lng <= maxLng;
            if (!inBounds) return false;
            return categoryName == null || categoryName.equalsIgnoreCase(category);
        }
    }

    // Configurable/Dynamic Geo Coverage Rules Repository
    private final List<GeoCoverageRule> dynamicCoverageRules = new ArrayList<>();

    public LocationRoutingEngine() {
        // Registered configurable geographical coverage bounding sectors
        // North Civic Division Coverage (Latitude 12.0 - 25.0, Longitude 70.0 - 85.0)
        dynamicCoverageRules.add(new GeoCoverageRule("Municipal Electrical Zone", 12.0, 25.0, 70.0, 85.0, "Streetlights", "Electrical & Street Lighting"));
        dynamicCoverageRules.add(new GeoCoverageRule("Municipal Sanitation Zone", 12.0, 25.0, 70.0, 85.0, "Sanitation/Garbage", "Sanitation & Waste Management"));
        dynamicCoverageRules.add(new GeoCoverageRule("Municipal Water Grid Zone", 12.0, 25.0, 70.0, 85.0, "Water Supply", "Water Supply & Sewerage Board"));
        dynamicCoverageRules.add(new GeoCoverageRule("Municipal Stormwater Network Zone", 12.0, 25.0, 70.0, 85.0, "Drainage", "Storm Water Drainage Board"));
        dynamicCoverageRules.add(new GeoCoverageRule("Municipal Road Works Division", 12.0, 25.0, 70.0, 85.0, "Road Maintenance", "Road Maintenance Department"));
    }

    public void registerGeoCoverageRule(GeoCoverageRule rule) {
        if (rule != null) {
            dynamicCoverageRules.add(rule);
        }
    }

    /**
     * Determines target department using actual GPS latitude & longitude and category.
     * Evaluates geographical coverage rules first. If no rule matches, falls back to Category-Department Mapping.
     */
    public RoutingResult routeComplaint(String categoryName, Double latitude, Double longitude) {
        // 1. Try GPS Location-Based Routing using actual coordinates
        if (latitude != null && longitude != null && isValidCoordinate(latitude, longitude)) {
            for (GeoCoverageRule rule : dynamicCoverageRules) {
                if (rule.matches(latitude, longitude, categoryName)) {
                    Optional<Department> dept = departmentRepository.findByName(rule.targetDepartmentName);
                    if (dept.isPresent()) {
                        logger.info("GPS Location ({}, {}) matched coverage rule '{}' -> Department: {}",
                                latitude, longitude, rule.zoneName, rule.targetDepartmentName);
                        return new RoutingResult(dept.get(), "LOCATION_BASED", rule.zoneName);
                    }
                }
            }
        }

        // 2. Category-Based Routing Fallback
        String targetDeptName = getCategoryDefaultDepartment(categoryName);
        Optional<Department> dept = departmentRepository.findByName(targetDeptName);
        Department finalDept = dept.orElseGet(() -> departmentRepository.findAll().stream().findFirst().orElse(null));

        return new RoutingResult(finalDept, "CATEGORY_BASED", null);
    }

    public static String getCategoryDefaultDepartment(String categoryName) {
        if (categoryName == null) return "Road Maintenance Department";
        switch (categoryName) {
            case "Road Maintenance":
                return "Road Maintenance Department";
            case "Streetlights":
                return "Electrical & Street Lighting";
            case "Sanitation/Garbage":
                return "Sanitation & Waste Management";
            case "Water Supply":
                return "Water Supply & Sewerage Board";
            case "Drainage":
                return "Storm Water Drainage Board";
            case "Other Urban Infrastructure":
            default:
                return "Road Maintenance Department";
        }
    }

    /**
     * Calculates Haversine distance in Kilometers between two GPS coordinates
     */
    public static double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c; // distance in kilometers
    }

    private boolean isValidCoordinate(Double lat, Double lon) {
        return lat != null && lon != null && lat >= -90.0 && lat <= 90.0 && lon >= -180.0 && lon <= 180.0 && (lat != 0.0 || lon != 0.0);
    }
}
