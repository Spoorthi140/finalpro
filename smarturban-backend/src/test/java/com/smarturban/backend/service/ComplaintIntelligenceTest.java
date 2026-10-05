package com.smarturban.backend.service;

import com.smarturban.backend.dto.ComplaintRequest;
import com.smarturban.backend.entity.*;
import com.smarturban.backend.repository.*;
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
    private LocationRoutingRuleRepository locationRoutingRuleRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    private User testUser;
    private Category roadCategory;
    private Department roadDepartment;
    private Department elecDepartment;

    @BeforeEach
    public void setUp() {
        complaintRepository.deleteAll();
        locationRoutingRuleRepository.deleteAll();

        if (userRepository.findByEmail("testcitizen@smarturban.com").isEmpty()) {
            User u = new User("Test Citizen", "testcitizen@smarturban.com", "9876543210", "Password@123", "Main St", "ROLE_CITIZEN");
            testUser = userRepository.save(u);
        } else {
            testUser = userRepository.findByEmail("testcitizen@smarturban.com").get();
        }

        roadDepartment = departmentRepository.findByName("Road Maintenance Department").orElseGet(() ->
                departmentRepository.save(new Department("Road Maintenance Department", "Road dept")));

        elecDepartment = departmentRepository.findByName("Electrical & Street Lighting").orElseGet(() ->
                departmentRepository.save(new Department("Electrical & Street Lighting", "Electrical dept")));

        roadCategory = categoryRepository.findByName("Road Maintenance").orElseGet(() ->
                categoryRepository.save(new Category("Road Maintenance", "Road issues", roadDepartment)));
    }

    @Test
    @DisplayName("1. Naive Bayes ML Classifier predicts Road, Streetlight, Garbage, Water, Drainage categories accurately")
    public void testAllCategoryPredictions() {
        // Road Maintenance
        ComplaintMLClassifier.ClassificationOutput roadRes = mlClassifier.classifyComplaint(
                "Large Pothole on Highway", "There is a massive crater in the asphalt tar pavement causing accidents.");
        assertEquals("Road Maintenance", roadRes.getCategoryName());
        assertFalse(roadRes.isLowConfidence());

        // Streetlights
        ComplaintMLClassifier.ClassificationOutput lightRes = mlClassifier.classifyComplaint(
                "Street Light Not Glowing", "Streetlight pole bulb fused dark road junction evening safety issue.");
        assertEquals("Streetlights", lightRes.getCategoryName());

        // Sanitation/Garbage
        ComplaintMLClassifier.ClassificationOutput saniRes = mlClassifier.classifyComplaint(
                "Overflowing Garbage Dustbin", "Trash scattered over street foul smell waste pile rotting.");
        assertEquals("Sanitation/Garbage", saniRes.getCategoryName());

        // Water Supply
        ComplaintMLClassifier.ClassificationOutput waterRes = mlClassifier.classifyComplaint(
                "Drinking Water Pipe Leak", "Main water supply pipeline burst dirty contaminated tap water.");
        assertEquals("Water Supply", waterRes.getCategoryName());

        // Drainage
        ComplaintMLClassifier.ClassificationOutput drainRes = mlClassifier.classifyComplaint(
                "Clogged Sewer Line", "Blocked storm drain overflowing sewage sludge stagnant water manhole open.");
        assertEquals("Drainage", drainRes.getCategoryName());
    }

    @Test
    @DisplayName("2. AI Classifier flags low confidence for ambiguous queries")
    public void testLowConfidenceHandling() {
        ComplaintMLClassifier.ClassificationOutput result = mlClassifier.classifyComplaint(
                "XYZ Ambiguous Input", "Abc 123 completely unrelated non-civic input.");

        assertEquals("Other Urban Infrastructure", result.getCategoryName());
        assertTrue(result.isLowConfidence());
    }

    @Test
    @DisplayName("3. Category-Based Routing correctly assigns category default department")
    public void testCategoryToDepartmentRouting() {
        LocationRoutingEngine.RoutingResult result = locationRoutingEngine.routeComplaint(
                "Road Maintenance", null, null);

        assertNotNull(result.getDepartment());
        assertEquals("Road Maintenance Department", result.getDepartment().getName());
        assertEquals("CATEGORY_BASED", result.getRoutingMethod());
    }

    @Test
    @DisplayName("4. DB-Driven Location-Based Routing matches registered LocationRoutingRule")
    public void testDbDrivenLocationBasedRouting() {
        // Register DB location rule
        LocationRoutingRule rule = new LocationRoutingRule(
                "Test Sector Rule", roadCategory, elecDepartment, 10.0, 20.0, 70.0, 80.0);
        locationRoutingRuleRepository.save(rule);

        LocationRoutingEngine.RoutingResult result = locationRoutingEngine.routeComplaint(
                "Road Maintenance", 15.0, 75.0);

        assertNotNull(result.getDepartment());
        assertEquals("Electrical & Street Lighting", result.getDepartment().getName());
        assertEquals("LOCATION_BASED", result.getRoutingMethod());
    }

    @Test
    @DisplayName("5. Semantic Duplicate Detection flags similar complaints within 500m")
    public void testDuplicateComplaintDetection() {
        ComplaintRequest req1 = new ComplaintRequest();
        req1.setTitle("Broken Water Pipeline");
        req1.setDescription("Water leaking heavily from main pipeline onto street.");
        req1.setCategoryId(roadCategory.getId());
        req1.setLatitude(15.0);
        req1.setLongitude(75.0);

        Complaint c1 = complaintService.createComplaint(testUser, req1, null);

        AIDuplicateDetector.DuplicateDetectionResult dup = aiDuplicateDetector.checkForDuplicates(
                "Burst Water Pipe Leaking", "Main water pipeline broken and leaking heavily on road.",
                c1.getCategory().getId(), 15.0001, 75.0001);

        assertTrue(dup.isPossibleDuplicate());
        assertTrue(dup.getSimilarityScore() >= 0.45);
        assertNotNull(dup.getRelatedComplaint());
        assertEquals(c1.getId(), dup.getRelatedComplaint().getId());
        assertNotNull(dup.getWarningReason());
    }

    @Test
    @DisplayName("6. Geographically distant complaints are NOT flagged as duplicates")
    public void testDistantComplaintNotDuplicate() {
        ComplaintRequest req1 = new ComplaintRequest();
        req1.setTitle("Overflowing Garbage Bin");
        req1.setDescription("Trash scattered all over street foul smell.");
        req1.setCategoryId(roadCategory.getId());
        req1.setLatitude(12.9716);
        req1.setLongitude(77.5946);

        complaintService.createComplaint(testUser, req1, null);

        AIDuplicateDetector.DuplicateDetectionResult dup = aiDuplicateDetector.checkForDuplicates(
                "Overflowing Garbage Bin", "Trash scattered all over street foul smell.",
                roadCategory.getId(), 25.0000, 85.0000);

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
