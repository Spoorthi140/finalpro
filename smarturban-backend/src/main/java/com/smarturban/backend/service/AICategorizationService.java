package com.smarturban.backend.service;

import com.smarturban.backend.entity.Category;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AICategorizationService {

    /**
     * Recommends a category ID based on the complaint title and description text.
     * This modular service can be integrated with external machine learning or NLP models.
     */
    public Long categorizeComplaintText(String title, String description) {
        String combined = ((title != null ? title : "") + " " + (description != null ? description : "")).toLowerCase(Locale.ROOT);

        if (combined.contains("pothole") || combined.contains("road") || combined.contains("asphalt") || combined.contains("tar")) {
            return 1L; // Road Maintenance
        } else if (combined.contains("light") || combined.contains("dark") || combined.contains("bulb") || combined.contains("pole")) {
            return 2L; // Streetlights
        } else if (combined.contains("garbage") || combined.contains("trash") || combined.contains("waste") || combined.contains("bin") || combined.contains("clean")) {
            return 3L; // Sanitation/Garbage
        } else if (combined.contains("water") || combined.contains("pipe") || combined.contains("leak") || combined.contains("tap")) {
            return 4L; // Water Supply
        } else if (combined.contains("drain") || combined.contains("sewer") || combined.contains("overflow") || combined.contains("gutter")) {
            return 5L; // Drainage
        }
        return 6L; // Other Urban Infrastructure
    }
}
