package com.smarturban.backend.service;

import com.smarturban.backend.dto.ComplaintRequest;
import com.smarturban.backend.entity.Category;
import com.smarturban.backend.entity.Complaint;
import com.smarturban.backend.entity.Department;
import com.smarturban.backend.entity.User;
import com.smarturban.backend.repository.CategoryRepository;
import com.smarturban.backend.repository.ComplaintRepository;
import com.smarturban.backend.repository.DepartmentRepository;
import com.smarturban.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ComplaintIntelligenceTest {

    @Autowired
    private ComplaintMLClassifier mlClassifier;

    @Autowired
    private LocationRoutingEngine locationRoutingEngine;

    @Autowired
    private AIDuplicateDetector aiDuplicateDetector;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    private User testUser;
    private Category roadCategory;
    private Department roadDepartment;

    @BeforeEach
    public void setUp() {
        complaintRepository.deleteAll();

        if (userRepository.findByEmail("testcitizen@smarturban.com").isEmpty()) {
            User u = new User("Test Citizen", "testcitizen@smarturban.com", "9876543210", "Password@123", "Main St", "ROLE_CITIZEN");
            testUser = userRepository.save(u);
        } else {
            testUser = userRepository.findByEmail("testcitizen@smarturban.com").get();
        }

        roadCategory = categoryRepository.findByName("Road Maintenance").orElseGet(() ->
                categoryRepository.save(new Category("Road Maintenance", "Road issues")));

        roadDepartment = departmentRepository.findByName("Road Maintenance Department").orElseGet(() ->
                departmentRepository.save(new Department("Road Maintenance Department", "Road dept")));
    }

    @Test
    @DisplayName("1. AI Classifier predicts category accurately with high confidence")
    public void testCategoryPrediction() {
        ComplaintMLClassifier.ClassificationOutput result = mlClassifier.classifyComplaint(
                "Large Pothole on Highway", "There is a massive crater in the asphalt tar pavement causing accidents.");

        assertEquals("Road Maintenance", result.getCategoryName());
        assertTrue(result.getConfidenceScore() >= 0.35);
        assertFalse(result.isLowConfidence());
    }

    @Test
    @DisplayName("2. AI Classifier falls back to low confidence for ambiguous queries")
    public void testLowConfidenceHandling() {
        ComplaintMLClassifier.ClassificationOutput result = mlClassifier.classifyComplaint(
                "XYZ Random Query", "Abc 123 nothing specific");

        assertEquals("Other Urban Infrastructure", result.getCategoryName());
        assertTrue(result.isLowConfidence());
    }

    @Test
    @DisplayName("3. Category-Based Routing correctly assigns default department")
    public void testCategoryToDepartmentRouting() {
        LocationRoutingEngine.RoutingResult result = locationRoutingEngine.routeComplaint(
                "Streetlights", null, null);

        assertNotNull(result.getDepartment());
        assertEquals("Electrical & Street Lighting", result.getDepartment().getName());
        assertEquals("CATEGORY_BASED", result.getRoutingMethod());
    }

    @Test
    @DisplayName("4. GPS Location-Based Routing matches geographical coverage rule")
    public void testLocationBasedRouting() {
        // GPS coordinates (15.0, 75.0) fall into Municipal Electrical Zone
        LocationRoutingEngine.RoutingResult result = locationRoutingEngine.routeComplaint(
                "Streetlights", 15.0, 75.0);

        assertNotNull(result.getDepartment());
        assertEquals("Electrical & Street Lighting", result.getDepartment().getName());
        assertEquals("LOCATION_BASED", result.getRoutingMethod());
    }

    @Test
    @DisplayName("5. Similar Complaint Detection flags potential duplicate within 500m")
    public void testDuplicateComplaintDetection() {
        // Create initial complaint
        ComplaintRequest req1 = new ComplaintRequest();
        req1.setTitle("Broken Water Pipeline");
        req1.setDescription("Water leaking heavily from main pipeline onto street.");
        req1.setCategoryId(roadCategory.getId());
        req1.setLatitude(15.0);
        req1.setLongitude(75.0);

        Complaint c1 = complaintService.createComplaint(testUser, req1, null);

        // Check duplicate for new similar complaint at nearly same location
        AIDuplicateDetector.DuplicateDetectionResult dup = aiDuplicateDetector.checkForDuplicates(
                "Burst Water Pipe Leaking", "Main water pipeline broken and leaking heavily on road.",
                c1.getCategory().getId(), 15.0001, 75.0001);

        assertTrue(dup.isPossibleDuplicate());
        assertTrue(dup.getSimilarityScore() >= 0.50);
        assertNotNull(dup.getRelatedComplaint());
        assertEquals(c1.getId(), dup.getRelatedComplaint().getId());
    }

    @Test
    @DisplayName("6. Complaints from different locations are NOT flagged as duplicates")
    public void testDifferentLocationNotDuplicate() {
        // Create complaint in Bangalore
        ComplaintRequest req1 = new ComplaintRequest();
        req1.setTitle("Overflowing Garbage Bin");
        req1.setDescription("Trash scattered all over street foul smell.");
        req1.setCategoryId(roadCategory.getId());
        req1.setLatitude(12.9716);
        req1.setLongitude(77.5946);

        complaintService.createComplaint(testUser, req1, null);

        // Check duplicate for similar complaint far away (> 20 km)
        AIDuplicateDetector.DuplicateDetectionResult dup = aiDuplicateDetector.checkForDuplicates(
                "Overflowing Garbage Bin", "Trash scattered all over street foul smell.",
                roadCategory.getId(), 13.1986, 77.7066);

        assertFalse(dup.isPossibleDuplicate());
    }

    @Test
    @DisplayName("7. Admin Category and Department overrides function correctly")
    public void testAdminOverrides() {
        ComplaintRequest req = new ComplaintRequest();
        req.setTitle("Broken Streetlight Lamp");
        req.setDescription("Dark street pole bulb fused.");
        req.setLatitude(12.9716);
        req.setLongitude(77.5946);

        Complaint c = complaintService.createComplaint(testUser, req, null);

        // Override category
        Complaint updated = complaintService.adminOverrideCategory(c.getId(), roadCategory.getId());
        assertEquals(roadCategory.getId(), updated.getCategory().getId());
        assertTrue(updated.getAdminCategoryOverridden());

        // Override department
        Complaint deptUpdated = complaintService.updateComplaintStatusAndDepartment(
                c.getId(), "In Progress", roadDepartment.getId(), "Admin", "Reassigned department");
        assertEquals(roadDepartment.getId(), deptUpdated.getDepartment().getId());
        assertTrue(deptUpdated.getAdminDepartmentOverridden());
    }
}
