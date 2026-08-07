package com.carebeforecare.prehospitalcare_platform.controller;

import com.carebeforecare.prehospitalcare_platform.model.entity.Medication;
import com.carebeforecare.prehospitalcare_platform.service.MedicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicationControllerUnitTest {  // ← Changed class name

    @Mock
    private MedicationService medicationService;

    @Mock
    private Model model;

    @InjectMocks
    private MedicationController medicationController;

    private Medication medication1;
    private Medication medication2;
    private List<Medication> medications;

    @BeforeEach
    void setUp() {
        medication1 = new Medication();
        medication1.setId(1L);
        medication1.setName("Aspirin");
        medication1.setDrugClass("NSAID");

        medication2 = new Medication();
        medication2.setId(2L);
        medication2.setName("Ibuprofen");
        medication2.setDrugClass("NSAID");

        medications = Arrays.asList(medication1, medication2);
    }

    @Test
    void getAllMedications_ShouldReturnMedicationsView() {
        // Arrange
        when(medicationService.getAllMedications()).thenReturn(medications);

        // Act
        String viewName = medicationController.getAllMedications(model);

        // Assert
        assertEquals("medications", viewName);
        verify(medicationService).getAllMedications();
        verify(model).addAttribute("medications", medications);
        verify(model).addAttribute("pageTitle", "Medications Guide");
    }

    @Test
    void getMedicationDetail_WithValidId_ShouldReturnMedicationDetailView() {
        // Arrange
        Long medicationId = 1L;
        when(medicationService.getMedicationById(medicationId)).thenReturn(medication1);

        // Act
        String viewName = medicationController.getMedicationDetail(medicationId, model);

        // Assert
        assertEquals("medication-detail", viewName);
        verify(medicationService).getMedicationById(medicationId);
        verify(model).addAttribute("medication", medication1);
        verify(model).addAttribute("pageTitle", medication1.getName() + " - Medication Details");
    }

    @Test
    void getMedicationDetail_WithInvalidId_ShouldRedirectToMedications() {
        // Arrange
        Long invalidId = 999L;
        when(medicationService.getMedicationById(invalidId))
            .thenThrow(new RuntimeException("Medication not found"));

        // Act
        String viewName = medicationController.getMedicationDetail(invalidId, model);

        // Assert
        assertEquals("redirect:/medications", viewName);
        verify(medicationService).getMedicationById(invalidId);
        verify(model).addAttribute("error", "Error loading medication details");
    }

    @Test
    void getMedicationsByDrugClass_ShouldReturnFilteredMedications() {
        // Arrange
        String drugClass = "NSAID";
        when(medicationService.getMedicationsByDrugClass(drugClass)).thenReturn(medications);

        // Act
        String viewName = medicationController.getMedicationsByDrugClass(drugClass, model);

        // Assert
        assertEquals("medications", viewName);
        verify(medicationService).getMedicationsByDrugClass(drugClass);
        verify(model).addAttribute("medications", medications);
        verify(model).addAttribute("drugClass", drugClass);
        verify(model).addAttribute("pageTitle", drugClass + " Medications");
    }

    @Test
    void getMedicationsByDrugClass_WithEmptyResult_ShouldReturnEmptyList() {
        // Arrange
        String drugClass = "UnknownClass";
        when(medicationService.getMedicationsByDrugClass(drugClass)).thenReturn(Arrays.asList());

        // Act
        String viewName = medicationController.getMedicationsByDrugClass(drugClass, model);

        // Assert
        assertEquals("medications", viewName);
        verify(medicationService).getMedicationsByDrugClass(drugClass);
        verify(model).addAttribute(eq("medications"), any(List.class));
        verify(model).addAttribute("drugClass", drugClass);
    }

    @Test
    void getAllMedications_WithEmptyList_ShouldHandleEmptyCase() {
        // Arrange
        when(medicationService.getAllMedications()).thenReturn(Arrays.asList());

        // Act
        String viewName = medicationController.getAllMedications(model);

        // Assert
        assertEquals("medications", viewName);
        verify(medicationService).getAllMedications();
        verify(model).addAttribute(eq("medications"), any(List.class));
    }
}