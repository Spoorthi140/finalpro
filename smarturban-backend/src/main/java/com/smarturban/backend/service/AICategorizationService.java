package com.smarturban.backend.service;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AICategorizationService {

    public static class ClassificationResult {
        private final String categoryName;
        private final String departmentName;

        public ClassificationResult(String categoryName, String departmentName) {
            this.categoryName = categoryName;
            this.departmentName = departmentName;
        }

        public String getCategoryName() { return categoryName; }
        public String getDepartmentName() { return departmentName; }
    }

    /**
     * Performs AI natural language classification on complaint title & description
     * to determine the best matching Category and responsible Department.
     */
    public ClassificationResult classifyAndRouteComplaint(String title, String description) {
        String text = ((title != null ? title : "") + " " + (description != null ? description : "")).toLowerCase(Locale.ROOT);

        if (text.contains("pothole") || text.contains("road") || text.contains("asphalt") || text.contains("tar") || text.contains("pavement") || text.contains("sidewalk")) {
            return new ClassificationResult("Road Maintenance", "Road Maintenance Department");
        } else if (text.contains("light") || text.contains("dark") || text.contains("bulb") || text.contains("pole") || text.contains("lamp") || text.contains("electricity")) {
            return new ClassificationResult("Streetlights", "Electrical & Street Lighting");
        } else if (text.contains("garbage") || text.contains("trash") || text.contains("waste") || text.contains("bin") || text.contains("clean") || text.contains("dump")) {
            return new ClassificationResult("Sanitation/Garbage", "Sanitation & Waste Management");
        } else if (text.contains("water") || text.contains("pipe") || text.contains("leak") || text.contains("tap") || text.contains("supply") || text.contains("contamination")) {
            return new ClassificationResult("Water Supply", "Water Supply & Sewerage Board");
        } else if (text.contains("drain") || text.contains("sewer") || text.contains("overflow") || text.contains("gutter") || text.contains("flooding") || text.contains("sludge")) {
            return new ClassificationResult("Drainage", "Storm Water Drainage Department");
        } else {
            return new ClassificationResult("Other Urban Infrastructure", "Road Maintenance Department");
        }
    }

    public Long categorizeComplaintText(String title, String description) {
        ClassificationResult result = classifyAndRouteComplaint(title, description);
        switch (result.getCategoryName()) {
            case "Road Maintenance": return 1L;
            case "Streetlights": return 2L;
            case "Sanitation/Garbage": return 3L;
            case "Water Supply": return 4L;
            case "Drainage": return 5L;
            default: return 6L;
        }
    }
}
