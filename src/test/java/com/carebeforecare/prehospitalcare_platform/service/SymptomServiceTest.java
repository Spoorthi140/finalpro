package com.carebeforecare.prehospitalcare_platform.service;

import com.carebeforecare.prehospitalcare_platform.model.entity.Symptom;
import com.carebeforecare.prehospitalcare_platform.repository.SymptomRepository;
import com.carebeforecare.prehospitalcare_platform.service.impl.SymptomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SymptomServiceTest {

    @Mock
    private SymptomRepository symptomRepository;

    private SymptomService symptomService;

    private Symptom symptom1;
    private Symptom symptom2;
    private Symptom symptom3;

    @BeforeEach
    void setUp() {
        symptomService = new SymptomServiceImpl(symptomRepository);

        symptom1 = new Symptom();
        symptom1.setId(1L);
        symptom1.setName("Headache");
        symptom1.setDescription("Pain in the head or upper neck");
        symptom1.setCategory("Neurological");
        symptom1.setSeverity("Medium");

        symptom2 = new Symptom();
        symptom2.setId(2L);
        symptom2.setName("Chest Pain");
        symptom2.setDescription("Pain or discomfort in the chest area");
        symptom2.setCategory("Cardiac");
        symptom2.setSeverity("High");


        symptom3 = new Symptom();
        symptom3.setId(3L);
        symptom3.setName("Fever");
        symptom3.setDescription("Elevated body temperature");
        symptom3.setCategory("General");
        symptom3.setSeverity("Low");

    }

    @Test
    void getAllSymptoms_ShouldReturnAllSymptoms() {
        // Arrange
        List<Symptom> expectedSymptoms = Arrays.asList(symptom1, symptom2, symptom3);
        when(symptomRepository.findAll()).thenReturn(expectedSymptoms);

        // Act
        List<Symptom> result = symptomService.getAllSymptoms();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Headache", result.get(0).getName());
        assertEquals("Chest Pain", result.get(1).getName());
        verify(symptomRepository).findAll();
    }

    @Test
    void getAllSymptoms_WhenEmpty_ShouldReturnEmptyList() {
        // Arrange
        when(symptomRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Symptom> result = symptomService.getAllSymptoms();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(symptomRepository).findAll();
    }

    @Test
    void getSymptomById_WithValidId_ShouldReturnSymptom() {
        // Arrange
        Long symptomId = 1L;
        when(symptomRepository.findById(symptomId)).thenReturn(Optional.of(symptom1));

        // Act
        Optional<Symptom> result = symptomService.getSymptomById(symptomId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Headache", result.get().getName());
        assertEquals("Neurological", result.get().getCategory());
        verify(symptomRepository).findById(symptomId);
    }

    @Test
    void getSymptomById_WithInvalidId_ShouldReturnEmpty() {
        // Arrange
        Long invalidId = 999L;
        when(symptomRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act
        Optional<Symptom> result = symptomService.getSymptomById(invalidId);

        // Assert
        assertFalse(result.isPresent());
        verify(symptomRepository).findById(invalidId);
    }

    @Test
    void getSymptomsByCategory_ShouldReturnFilteredSymptoms() {
        // Arrange
        String category = "Neurological";
        List<Symptom> expectedSymptoms = Arrays.asList(symptom1);
        when(symptomRepository.findByCategory(category)).thenReturn(expectedSymptoms);

        // Act
        List<Symptom> result = symptomService.getSymptomsByCategory(category);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.stream().allMatch(symptom -> category.equals(symptom.getCategory())));
        verify(symptomRepository).findByCategory(category);
    }

    @Test
    void getSymptomsByCategory_WithNonExistingCategory_ShouldReturnEmptyList() {
        // Arrange
        String nonExistingCategory = "NonExisting";
        when(symptomRepository.findByCategory(nonExistingCategory)).thenReturn(Arrays.asList());

        // Act
        List<Symptom> result = symptomService.getSymptomsByCategory(nonExistingCategory);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(symptomRepository).findByCategory(nonExistingCategory);
    }

    @Test
    void searchSymptoms_WithKeyword_ShouldReturnMatchingSymptoms() {
        // Arrange
        String keyword = "pain";
        List<Symptom> expectedSymptoms = Arrays.asList(symptom1, symptom2);
        when(symptomRepository.searchSymptoms(keyword)).thenReturn(expectedSymptoms);

        // Act
        List<Symptom> result = symptomService.searchSymptoms(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(symptomRepository).searchSymptoms(keyword);
    }

    @Test
    void searchSymptoms_WithEmptyKeyword_ShouldReturnAllSymptoms() {
        // Arrange
        String keyword = "";
        List<Symptom> expectedSymptoms = Arrays.asList(symptom1, symptom2, symptom3);
        when(symptomRepository.findAll()).thenReturn(expectedSymptoms);

        // Act
        List<Symptom> result = symptomService.searchSymptoms(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(symptomRepository).findAll();
    }

    @Test
    void searchSymptoms_WithNullKeyword_ShouldReturnAllSymptoms() {
        // Arrange
        List<Symptom> expectedSymptoms = Arrays.asList(symptom1, symptom2, symptom3);
        when(symptomRepository.findAll()).thenReturn(expectedSymptoms);

        // Act
        List<Symptom> result = symptomService.searchSymptoms(null);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(symptomRepository).findAll();
    }

    @Test
    void saveSymptom_ShouldReturnSavedSymptom() {
        // Arrange
        Symptom newSymptom = new Symptom();
        newSymptom.setName("Nausea");
        newSymptom.setCategory("Digestive");
        newSymptom.setDescription("Feeling of sickness with an inclination to vomit");

        when(symptomRepository.save(newSymptom)).thenReturn(newSymptom);

        // Act
        Symptom result = symptomService.saveSymptom(newSymptom);

        // Assert
        assertNotNull(result);
        assertEquals("Nausea", result.getName());
        assertEquals("Digestive", result.getCategory());
        verify(symptomRepository).save(newSymptom);
    }

    @Test
    void updateSymptom_WithValidId_ShouldReturnUpdatedSymptom() {
        // Arrange
        Long symptomId = 1L;
        Symptom symptomDetails = new Symptom();
        symptomDetails.setName("Updated Headache");
        symptomDetails.setDescription("Updated description");
        symptomDetails.setCategory("Updated Neurological");
        symptomDetails.setSeverity("High");


        when(symptomRepository.findById(symptomId)).thenReturn(Optional.of(symptom1));
        when(symptomRepository.save(any(Symptom.class))).thenReturn(symptom1);

        // Act
        Symptom result = symptomService.updateSymptom(symptomId, symptomDetails);

        // Assert
        assertNotNull(result);
        verify(symptomRepository).findById(symptomId);
        verify(symptomRepository).save(any(Symptom.class));
    }

    @Test
    void updateSymptom_WithInvalidId_ShouldThrowException() {
        // Arrange
        Long invalidId = 999L;
        Symptom symptomDetails = new Symptom();
        symptomDetails.setName("Updated Symptom");

        when(symptomRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            symptomService.updateSymptom(invalidId, symptomDetails);
        });

        assertEquals("Symptom not found with id: " + invalidId, exception.getMessage());
        verify(symptomRepository).findById(invalidId);
        verify(symptomRepository, never()).save(any(Symptom.class));
    }

    @Test
    void deleteSymptom_WithValidId_ShouldDeleteSymptom() {
        // Arrange
        Long symptomId = 1L;
        when(symptomRepository.findById(symptomId)).thenReturn(Optional.of(symptom1));
        doNothing().when(symptomRepository).delete(symptom1);

        // Act
        symptomService.deleteSymptom(symptomId);

        // Assert
        verify(symptomRepository).findById(symptomId);
        verify(symptomRepository).delete(symptom1);
    }

    @Test
    void deleteSymptom_WithInvalidId_ShouldThrowException() {
        // Arrange
        Long invalidId = 999L;
        when(symptomRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            symptomService.deleteSymptom(invalidId);
        });

        assertEquals("Symptom not found with id: " + invalidId, exception.getMessage());
        verify(symptomRepository).findById(invalidId);
        verify(symptomRepository, never()).delete(any(Symptom.class));
    }

    @Test
    void countSymptoms_ShouldReturnCount() {
        // Arrange
        long expectedCount = 3L;
        when(symptomRepository.count()).thenReturn(expectedCount);

        // Act
        long result = symptomService.countSymptoms();

        // Assert
        assertEquals(expectedCount, result);
        verify(symptomRepository).count();
    }

    @Test
    void getAllCategories_ShouldReturnUniqueCategories() {
        // Arrange
        List<String> expectedCategories = Arrays.asList("Neurological", "Cardiac", "General", "Respiratory");
        when(symptomRepository.findDistinctCategories()).thenReturn(expectedCategories);

        // Act
        List<String> result = symptomService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.contains("Neurological"));
        assertTrue(result.contains("Cardiac"));
        verify(symptomRepository).findDistinctCategories();
    }

    @Test
    void getAllCategories_WhenNoCategories_ShouldReturnEmptyList() {
        // Arrange
        when(symptomRepository.findDistinctCategories()).thenReturn(Arrays.asList());

        // Act
        List<String> result = symptomService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(symptomRepository).findDistinctCategories();
    }
}