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
        private final String routingMethod; // "LOCATION_BASED" or "CATEGORY_BASED"
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

    // Geographical Department Sector Zones (e.g. Municipal Ward / Zonal Divisions)
    public static class GeographicalZone {
        private final String zoneName;
        private final double centerLat;
        private final double centerLng;
        private final double radiusKm;
        private final String departmentName;

        public GeographicalZone(String zoneName, double centerLat, double centerLng, double radiusKm, String departmentName) {
            this.zoneName = zoneName;
            this.centerLat = centerLat;
            this.centerLng = centerLng;
            this.radiusKm = radiusKm;
            this.departmentName = departmentName;
        }

        public boolean containsPoint(double lat, double lng) {
            double distance = calculateHaversineDistance(centerLat, centerLng, lat, lng);
            return distance <= radiusKm;
        }
    }

    // Sample Geo-Sectors (Bangalore / Metropolitan Civic Zones)
    private static final List<GeographicalZone> SPECIALIZED_GEO_ZONES = Arrays.asList(
            new GeographicalZone("North Electrical Sub-Zone", 13.0358, 77.5970, 5.0, "Electrical & Street Lighting"),
            new GeographicalZone("East Water Supply Grid", 12.9716, 77.5946, 8.0, "Water Supply & Sewerage Board"),
            new GeographicalZone("Central Stormwater Division", 12.9784, 77.6408, 6.0, "Storm Water Drainage Board"),
            new GeographicalZone("South Sanitation Sector", 12.9250, 77.5898, 7.0, "Sanitation & Waste Management")
    );

    /**
     * Determines the target department for a complaint using actual GPS latitude & longitude.
     * Falls back to Category-Department Mapping if GPS location is absent or outside special sectors.
     */
    public RoutingResult routeComplaint(String categoryName, Double latitude, Double longitude) {
        // 1. Try GPS Location-Based Routing if latitude and longitude are valid
        if (latitude != null && longitude != null && isValidCoordinate(latitude, longitude)) {
            for (GeographicalZone zone : SPECIALIZED_GEO_ZONES) {
                if (zone.containsPoint(latitude, longitude)) {
                    Optional<Department> dept = departmentRepository.findByName(zone.departmentName);
                    if (dept.isPresent()) {
                        logger.info("GPS Location ({}, {}) routed to sector zone '{}' -> Department: {}",
                                latitude, longitude, zone.zoneName, zone.departmentName);
                        return new RoutingResult(dept.get(), "LOCATION_BASED", zone.zoneName);
                    }
                }
            }
        }

        // 2. Category-Based Routing Fallback Matrix
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
