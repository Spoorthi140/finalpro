package com.carebeforecare.prehospitalcare_platform.service;

import com.carebeforecare.prehospitalcare_platform.model.entity.FirstAidGuide;
import com.carebeforecare.prehospitalcare_platform.service.impl.FirstAidServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirstAidServiceTest {

    @Mock
    private com.carebeforecare.prehospitalcare_platform.repository.FirstAidGuideRepository firstAidGuideRepository;

    private FirstAidService firstAidService;

    private FirstAidGuide guide1;
    private FirstAidGuide guide2;
    private FirstAidGuide guide3;

    @BeforeEach
    void setUp() {
        firstAidService = new FirstAidServiceImpl(firstAidGuideRepository);

        guide1 = new FirstAidGuide();
        guide1.setId(1L);
        guide1.setTitle("CPR Basics");
        guide1.setDescription("Basic CPR instructions");
        guide1.setSteps("Step 1: Check safety\nStep 2: Call for help\nStep 3: Start compressions");
        guide1.setCategory("Emergency");
        guide1.setDifficulty("Beginner");
        guide1.setSeverity("High");

        guide1.setEquipmentNeeded("None");
        guide1.setIcon("cpr-icon");

        guide2 = new FirstAidGuide();
        guide2.setId(2L);
        guide2.setTitle("Bleeding Control");
        guide2.setDescription("How to control bleeding");
        guide2.setSteps("Step 1: Apply pressure\nStep 2: Elevate wound\nStep 3: Bandage");
        guide2.setCategory("Wound Care");
        guide2.setDifficulty("Beginner");
        guide2.setSeverity("Medium");

        guide2.setEquipmentNeeded("Gloves, Bandage");
        guide2.setIcon("bleeding-icon");

        guide3 = new FirstAidGuide();
        guide3.setId(3L);
        guide3.setTitle("Advanced CPR");
        guide3.setDescription("Advanced CPR techniques");
        guide3.setSteps("Step 1: Advanced techniques\nStep 2: Use of AED");
        guide3.setCategory("Emergency");
        guide3.setDifficulty("Advanced");
        guide3.setSeverity("High");

        guide3.setEquipmentNeeded("AED");
        guide3.setIcon("advanced-cpr-icon");
    }

    @Test
    void getAllFirstAidGuides_ShouldReturnAllGuides() {
        // Arrange
        List<FirstAidGuide> expectedGuides = Arrays.asList(guide1, guide2, guide3);
        when(firstAidGuideRepository.findAll()).thenReturn(expectedGuides);

        // Act
        List<FirstAidGuide> result = firstAidService.getAllFirstAidGuides();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("CPR Basics", result.get(0).getTitle());
        assertEquals("Bleeding Control", result.get(1).getTitle());
        verify(firstAidGuideRepository).findAll();
    }

    @Test
    void getAllFirstAidGuides_WhenEmpty_ShouldReturnEmptyList() {
        // Arrange
        when(firstAidGuideRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<FirstAidGuide> result = firstAidService.getAllFirstAidGuides();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(firstAidGuideRepository).findAll();
    }

    @Test
    void getFirstAidGuideById_WithValidId_ShouldReturnGuide() {
        // Arrange
        Long guideId = 1L;
        when(firstAidGuideRepository.findById(guideId)).thenReturn(Optional.of(guide1));

        // Act
        Optional<FirstAidGuide> result = firstAidService.getFirstAidGuideById(guideId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("CPR Basics", result.get().getTitle());
        assertEquals("Emergency", result.get().getCategory());
        verify(firstAidGuideRepository).findById(guideId);
    }

    @Test
    void getFirstAidGuideById_WithInvalidId_ShouldReturnEmpty() {
        // Arrange
        Long invalidId = 999L;
        when(firstAidGuideRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act
        Optional<FirstAidGuide> result = firstAidService.getFirstAidGuideById(invalidId);

        // Assert
        assertFalse(result.isPresent());
        verify(firstAidGuideRepository).findById(invalidId);
    }

    @Test
    void getFirstAidGuidesByCategory_ShouldReturnFilteredGuides() {
        // Arrange
        String category = "Emergency";
        List<FirstAidGuide> expectedGuides = Arrays.asList(guide1, guide3);
        when(firstAidGuideRepository.findByCategory(category)).thenReturn(expectedGuides);

        // Act
        List<FirstAidGuide> result = firstAidService.getFirstAidGuidesByCategory(category);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(guide -> category.equals(guide.getCategory())));
        verify(firstAidGuideRepository).findByCategory(category);
    }

    @Test
    void getFirstAidGuidesByCategory_WithNonExistingCategory_ShouldReturnEmptyList() {
        // Arrange
        String nonExistingCategory = "NonExisting";
        when(firstAidGuideRepository.findByCategory(nonExistingCategory)).thenReturn(Arrays.asList());

        // Act
        List<FirstAidGuide> result = firstAidService.getFirstAidGuidesByCategory(nonExistingCategory);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(firstAidGuideRepository).findByCategory(nonExistingCategory);
    }

    @Test
    void searchFirstAidGuides_WithKeyword_ShouldReturnMatchingGuides() {
        // Arrange
        String keyword = "CPR";
        List<FirstAidGuide> expectedGuides = Arrays.asList(guide1, guide3);
        when(firstAidGuideRepository.searchFirstAidGuides(keyword)).thenReturn(expectedGuides);

        // Act
        List<FirstAidGuide> result = firstAidService.searchFirstAidGuides(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(guide -> guide.getTitle().contains("CPR")));
        verify(firstAidGuideRepository).searchFirstAidGuides(keyword);
    }

    @Test
    void searchFirstAidGuides_WithEmptyKeyword_ShouldCallRepository() {
        // Arrange
        String keyword = "";
        List<FirstAidGuide> expectedGuides = Arrays.asList(guide1, guide2, guide3);
        when(firstAidGuideRepository.searchFirstAidGuides(keyword)).thenReturn(expectedGuides);

        // Act
        List<FirstAidGuide> result = firstAidService.searchFirstAidGuides(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(firstAidGuideRepository).searchFirstAidGuides(keyword);
    }

    @Test
    void getRelatedFirstAidGuides_ShouldReturnRelatedGuidesExcludingCurrent() {
        // Arrange
        String category = "Emergency";
        Long excludeId = 1L;
        List<FirstAidGuide> expectedGuides = Arrays.asList(guide3);
        when(firstAidGuideRepository.findByCategoryAndIdNot(category, excludeId)).thenReturn(expectedGuides);

        // Act
        List<FirstAidGuide> result = firstAidService.getRelatedFirstAidGuides(category, excludeId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Advanced CPR", result.get(0).getTitle());
        assertNotEquals(excludeId, result.get(0).getId());
        verify(firstAidGuideRepository).findByCategoryAndIdNot(category, excludeId);
    }

    @Test
    void getRelatedFirstAidGuides_WithNoRelatedGuides_ShouldReturnEmptyList() {
        // Arrange
        String category = "Emergency";
        Long excludeId = 1L;
        when(firstAidGuideRepository.findByCategoryAndIdNot(category, excludeId)).thenReturn(Arrays.asList());

        // Act
        List<FirstAidGuide> result = firstAidService.getRelatedFirstAidGuides(category, excludeId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(firstAidGuideRepository).findByCategoryAndIdNot(category, excludeId);
    }

    @Test
    void saveFirstAidGuide_ShouldReturnSavedGuide() {
        // Arrange
        FirstAidGuide newGuide = new FirstAidGuide();
        newGuide.setTitle("New Guide");
        newGuide.setCategory("New Category");
        newGuide.setDescription("New description");

        when(firstAidGuideRepository.save(newGuide)).thenReturn(newGuide);

        // Act
        FirstAidGuide result = firstAidService.saveFirstAidGuide(newGuide);

        // Assert
        assertNotNull(result);
        assertEquals("New Guide", result.getTitle());
        assertEquals("New Category", result.getCategory());
        verify(firstAidGuideRepository).save(newGuide);
    }

    @Test
    void updateFirstAidGuide_WithValidId_ShouldReturnUpdatedGuide() {
        // Arrange
        Long guideId = 1L;
        FirstAidGuide guideDetails = new FirstAidGuide();
        guideDetails.setTitle("Updated CPR");
        guideDetails.setDescription("Updated description");
        guideDetails.setSteps("Updated steps");
        guideDetails.setCategory("Updated Emergency");
        guideDetails.setDifficulty("Expert");
        guideDetails.setSeverity("Critical");
        guideDetails.setEquipmentNeeded("AED, Gloves");
        guideDetails.setIcon("updated-icon");

        when(firstAidGuideRepository.findById(guideId)).thenReturn(Optional.of(guide1));
        when(firstAidGuideRepository.save(any(FirstAidGuide.class))).thenReturn(guide1);

        // Act
        FirstAidGuide result = firstAidService.updateFirstAidGuide(guideId, guideDetails);

        // Assert
        assertNotNull(result);
        verify(firstAidGuideRepository).findById(guideId);
        verify(firstAidGuideRepository).save(any(FirstAidGuide.class));
    }

    @Test
    void updateFirstAidGuide_WithInvalidId_ShouldThrowException() {
        // Arrange
        Long invalidId = 999L;
        FirstAidGuide guideDetails = new FirstAidGuide();
        guideDetails.setTitle("Updated Guide");

        when(firstAidGuideRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            firstAidService.updateFirstAidGuide(invalidId, guideDetails);
        });

        assertEquals("First aid guide not found with id: " + invalidId, exception.getMessage());
        verify(firstAidGuideRepository).findById(invalidId);
        verify(firstAidGuideRepository, never()).save(any(FirstAidGuide.class));
    }

    @Test
    void deleteFirstAidGuide_WithValidId_ShouldDeleteGuide() {
        // Arrange
        Long guideId = 1L;
        when(firstAidGuideRepository.findById(guideId)).thenReturn(Optional.of(guide1));
        doNothing().when(firstAidGuideRepository).delete(guide1);

        // Act
        firstAidService.deleteFirstAidGuide(guideId);

        // Assert
        verify(firstAidGuideRepository).findById(guideId);
        verify(firstAidGuideRepository).delete(guide1);
    }

    @Test
    void deleteFirstAidGuide_WithInvalidId_ShouldThrowException() {
        // Arrange
        Long invalidId = 999L;
        when(firstAidGuideRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            firstAidService.deleteFirstAidGuide(invalidId);
        });

        assertEquals("First aid guide not found with id: " + invalidId, exception.getMessage());
        verify(firstAidGuideRepository).findById(invalidId);
        verify(firstAidGuideRepository, never()).delete(any(FirstAidGuide.class));
    }

    @Test
    void countFirstAidGuides_ShouldReturnCount() {
        // Arrange
        long expectedCount = 3L;
        when(firstAidGuideRepository.count()).thenReturn(expectedCount);

        // Act
        long result = firstAidService.countFirstAidGuides();

        // Assert
        assertEquals(expectedCount, result);
        verify(firstAidGuideRepository).count();
    }

    @Test
    void getAllCategories_ShouldReturnUniqueCategories() {
        // Arrange
        List<String> expectedCategories = Arrays.asList("Emergency", "Wound Care", "Burns", "Fractures");
        when(firstAidGuideRepository.findDistinctCategories()).thenReturn(expectedCategories);

        // Act
        List<String> result = firstAidService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.contains("Emergency"));
        assertTrue(result.contains("Wound Care"));
        verify(firstAidGuideRepository).findDistinctCategories();
    }

    @Test
    void getAllCategories_WhenNoCategories_ShouldReturnEmptyList() {
        // Arrange
        when(firstAidGuideRepository.findDistinctCategories()).thenReturn(Arrays.asList());

        // Act
        List<String> result = firstAidService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(firstAidGuideRepository).findDistinctCategories();
    }

    @Test
    void getAllFirstAidGuides_ShouldHandleNullResults() {
        // Arrange
        when(firstAidGuideRepository.findAll()).thenReturn(null);

        // Act
        List<FirstAidGuide> result = firstAidService.getAllFirstAidGuides();

        // Assert
        assertNull(result);
        verify(firstAidGuideRepository).findAll();
    }

    @Test
    void searchFirstAidGuides_WithNullKeyword_ShouldHandleGracefully() {
        // Arrange
        when(firstAidGuideRepository.searchFirstAidGuides(null)).thenReturn(Arrays.asList(guide1, guide2));

        // Act
        List<FirstAidGuide> result = firstAidService.searchFirstAidGuides(null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(firstAidGuideRepository).searchFirstAidGuides(null);
    }
}