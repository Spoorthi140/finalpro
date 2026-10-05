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
            // Seed Departments first
            Department roadDept = null;
            Department elecDept = null;
            Department saniDept = null;
            Department waterDept = null;
            Department drainDept = null;

            if (departmentRepository.count() == 0) {
                roadDept = departmentRepository.save(new Department("Road Maintenance Department", "Handles road repairs, paving, and sidewalk maintenance."));
                elecDept = departmentRepository.save(new Department("Electrical & Street Lighting", "Handles municipal streetlights and power distribution infrastructure."));
                saniDept = departmentRepository.save(new Department("Sanitation & Waste Management", "Handles solid waste collection, street sweeping, and sanitation."));
                waterDept = departmentRepository.save(new Department("Water Supply & Sewerage Board", "Handles drinking water distribution and sewage pipelines."));
                drainDept = departmentRepository.save(new Department("Storm Water Drainage Department", "Handles municipal drainage systems and flood prevention."));
            } else {
                roadDept = departmentRepository.findByName("Road Maintenance Department").orElse(null);
                elecDept = departmentRepository.findByName("Electrical & Street Lighting").orElse(null);
                saniDept = departmentRepository.findByName("Sanitation & Waste Management").orElse(null);
                waterDept = departmentRepository.findByName("Water Supply & Sewerage Board").orElse(null);
                drainDept = departmentRepository.findByName("Storm Water Drainage Department").orElse(null);
            }

            // Seed Categories with Default Departments
            if (categoryRepository.count() == 0) {
                categoryRepository.save(new Category("Road Maintenance", "Issues related to damaged roads, potholes, asphalt, and sidewalks.", roadDept));
                categoryRepository.save(new Category("Streetlights", "Non-functional, broken, or flickering streetlight fixtures.", elecDept));
                categoryRepository.save(new Category("Sanitation/Garbage", "Uncollected garbage, overflow bins, and street cleaning issues.", saniDept));
                categoryRepository.save(new Category("Water Supply", "Water leakage, contamination, or low pressure supply.", waterDept));
                categoryRepository.save(new Category("Drainage", "Blocked drains, overflowing sewage, and open gutters.", drainDept));
                categoryRepository.save(new Category("Other Urban Infrastructure", "Parks, bus shelters, public toilets, and other civic infrastructure.", roadDept));
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
