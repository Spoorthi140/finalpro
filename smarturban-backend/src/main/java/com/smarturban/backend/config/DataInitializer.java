package com.smarturban.backend.config;

import com.smarturban.backend.entity.Category;
import com.smarturban.backend.entity.Department;
import com.smarturban.backend.entity.User;
import com.smarturban.backend.repository.CategoryRepository;
import com.smarturban.backend.repository.DepartmentRepository;
import com.smarturban.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Value("${admin.email:admin@smarturban.com}")
    private String adminEmail;

    @Value("${admin.password:Admin@123}")
    private String adminPassword;

    @Value("${admin.full-name:System Administrator}")
    private String adminFullName;

    @Value("${admin.phone:9876543210}")
    private String adminPhone;

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed Categories
            if (categoryRepository.count() == 0) {
                categoryRepository.save(new Category("Road Maintenance", "Issues related to damaged roads, potholes, asphalt, and sidewalks."));
                categoryRepository.save(new Category("Streetlights", "Non-functional, broken, or flickering streetlight fixtures."));
                categoryRepository.save(new Category("Sanitation/Garbage", "Uncollected garbage, overflow bins, and street cleaning issues."));
                categoryRepository.save(new Category("Water Supply", "Water leakage, contamination, or low pressure supply."));
                categoryRepository.save(new Category("Drainage", "Blocked drains, overflowing sewage, and open gutters."));
                categoryRepository.save(new Category("Other Urban Infrastructure", "Parks, bus shelters, public toilets, and other civic infrastructure."));
            }

            // Seed Departments
            if (departmentRepository.count() == 0) {
                departmentRepository.save(new Department("Road Maintenance Department", "Handles road repairs, paving, and sidewalk maintenance."));
                departmentRepository.save(new Department("Electrical & Street Lighting", "Handles municipal streetlights and power distribution infrastructure."));
                departmentRepository.save(new Department("Sanitation & Waste Management", "Handles solid waste collection, street sweeping, and sanitation."));
                departmentRepository.save(new Department("Water Supply & Sewerage Board", "Handles drinking water distribution and sewage pipelines."));
                departmentRepository.save(new Department("Storm Water Drainage Department", "Handles municipal drainage systems and flood prevention."));
            }

            // Seed Configured Admin Account
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = new User(
                        adminFullName,
                        adminEmail,
                        adminPhone,
                        passwordEncoder.encode(adminPassword),
                        "SmartUrban HQ, City Center",
                        "ROLE_ADMIN"
                );
                userRepository.save(admin);
            }
        };
    }
}
