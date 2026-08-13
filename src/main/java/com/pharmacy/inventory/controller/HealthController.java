package com.pharmacy.inventory.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/database")
    public Map<String, Object> checkDatabase() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Test database connection
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            response.put("status", "UP");
            response.put("database", "Connected");
            response.put("timestamp", java.time.LocalDateTime.now());
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("database", "Disconnected");
            response.put("error", e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now());
        }
        return response;
    }

    @GetMapping("/stats")
    public Map<String, Object> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            // Get counts from each table
            Long medicationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM medications", Long.class);
            Long categoryCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM categories", Long.class);
            Long supplierCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM suppliers", Long.class);

            stats.put("medications", medicationCount);
            stats.put("categories", categoryCount);
            stats.put("suppliers", supplierCount);
            stats.put("timestamp", java.time.LocalDateTime.now());
        } catch (Exception e) {
            stats.put("error", "Unable to fetch statistics: " + e.getMessage());
        }
        return stats;
    }
}