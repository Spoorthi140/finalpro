package com.smarturban.backend.service;

import com.smarturban.backend.entity.Category;
import com.smarturban.backend.entity.Department;
import com.smarturban.backend.entity.LocationRoutingRule;
import com.smarturban.backend.repository.CategoryRepository;
import com.smarturban.backend.repository.DepartmentRepository;
import com.smarturban.backend.repository.LocationRoutingRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LocationRoutingEngine {

    private static final Logger logger = LoggerFactory.getLogger(LocationRoutingEngine.class);

    @Autowired
    private LocationRoutingRuleRepository locationRoutingRuleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public static class RoutingResult {
        private final Department department;
        private final String routingMethod; // "LOCATION_BASED", "CATEGORY_BASED", "MANUAL_OVERRIDE"
        private final String matchedRuleName;

        public RoutingResult(Department department, String routingMethod, String matchedRuleName) {
            this.department = department;
            this.routingMethod = routingMethod;
            this.matchedRuleName = matchedRuleName;
        }

        public Department getDepartment() { return department; }
        public String getRoutingMethod() { return routingMethod; }
        public String getMatchedRuleName() { return matchedRuleName; }
    }

    /**
     * Determines target department using actual GPS latitude & longitude and category.
     * Evaluates database-driven location routing rules first.
     * If no rule matches, falls back to category default department.
     */
    public RoutingResult routeComplaint(String categoryName, Double latitude, Double longitude) {
        // 1. Try DB-driven GPS Location-Based Routing if latitude and longitude are valid
        if (latitude != null && longitude != null && isValidCoordinate(latitude, longitude)) {
            List<LocationRoutingRule> activeRules = locationRoutingRuleRepository.findByEnabledTrue();
            for (LocationRoutingRule rule : activeRules) {
                if (rule.matches(latitude, longitude, categoryName)) {
                    logger.info("GPS Location ({}, {}) matched DB routing rule '{}' -> Department: {}",
                            latitude, longitude, rule.getRuleName(), rule.getTargetDepartment().getName());
                    return new RoutingResult(rule.getTargetDepartment(), "LOCATION_BASED", rule.getRuleName());
                }
            }
        }

        // 2. Category Default Department Fallback
        if (categoryName != null) {
            Optional<Category> catOpt = categoryRepository.findByName(categoryName);
            if (catOpt.isPresent()) {
                Department defaultDept = catOpt.get().getDefaultDepartment();
                if (defaultDept != null) {
                    return new RoutingResult(defaultDept, "CATEGORY_BASED", null);
                }
            }
        }

        // 3. System Fallback - Leave unassigned if no category default department is configured
        return new RoutingResult(null, "UNASSIGNED", null);
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

    public static boolean isValidCoordinate(Double lat, Double lon) {
        return lat != null && lon != null && lat >= -90.0 && lat <= 90.0 && lon >= -180.0 && lon <= 180.0 && (lat != 0.0 || lon != 0.0);
    }
}
