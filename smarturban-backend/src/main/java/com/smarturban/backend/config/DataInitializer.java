package com.smarturban.backend.config;

import com.smarturban.backend.entity.Category;
import com.smarturban.backend.entity.Department;
import com.smarturban.backend.entity.User;
import com.smarturban.backend.repository.CategoryRepository;
import com.smarturban.backend.repository.DepartmentRepository;
import com.smarturban.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

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

            // Seed Fixed Admin Account: admin@smarturban.com / Admin@123
            if (!userRepository.existsByEmail("admin@smarturban.com")) {
                User admin = new User(
                        "System Administrator",
                        "admin@smarturban.com",
                        "9876543210",
                        passwordEncoder.encode("Admin@123"),
                        "SmartUrban HQ, City Center",
                        "ROLE_ADMIN"
                );
                userRepository.save(admin);
            }

            // Seed Sample Citizen Account
            if (!userRepository.existsByEmail("citizen@smarturban.com")) {
                User citizen = new User(
                        "John Citizen",
                        "citizen@smarturban.com",
                        "9123456789",
                        passwordEncoder.encode("Citizen@123"),
                        "123 MG Road, Sector 4",
                        "ROLE_CITIZEN"
                );
                userRepository.save(citizen);
            }
        };
    }
}
