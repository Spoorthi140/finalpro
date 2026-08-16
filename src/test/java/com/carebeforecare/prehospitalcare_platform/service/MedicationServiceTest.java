package com.carebeforecare.prehospitalcare_platform.service;

import com.carebeforecare.prehospitalcare_platform.model.entity.Medication;
import com.carebeforecare.prehospitalcare_platform.repository.MedicationRepository;
import com.carebeforecare.prehospitalcare_platform.service.impl.MedicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicationServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private MedicationServiceImpl medicationService;

    private Medication paracetamol;
    private Medication ibuprofen;

    @BeforeEach
    void setUp() {
        paracetamol = new Medication();
        paracetamol.setId(1L);
        paracetamol.setName("Paracetamol");
        paracetamol.setGenericName("Acetaminophen");
        paracetamol.setDosageForms("Tablets, Syrup");
        paracetamol.setCommonUses("Pain relief, Fever reduction");
        paracetamol.setSideEffects("Liver damage in overdose");
        paracetamol.setPrecautions("Do not exceed recommended dose");
        paracetamol.setUsageInstructions("Take every 4-6 hours");
        paracetamol.setDrugClass("Analgesic");
        paracetamol.setPrescriptionRequired(false);
        paracetamol.setStorageInstructions("Room temperature");
        paracetamol.setCreatedAt(LocalDateTime.now());

        ibuprofen = new Medication();
        ibuprofen.setId(2L);
        ibuprofen.setName("Ibuprofen");
        ibuprofen.setGenericName("Ibuprofen");
        ibuprofen.setDosageForms("Tablets, Capsules");
        ibuprofen.setCommonUses("Pain, Inflammation, Fever");
        ibuprofen.setSideEffects("Stomach upset");
        ibuprofen.setPrecautions("Take with food");
        ibuprofen.setUsageInstructions("Take every 6-8 hours");
        ibuprofen.setDrugClass("NSAID");
        ibuprofen.setPrescriptionRequired(false);
        ibuprofen.setStorageInstructions("Room temperature");
        ibuprofen.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testGetAllMedications() {
        // Arrange
        when(medicationRepository.findAll()).thenReturn(Arrays.asList(paracetamol, ibuprofen));

        // Act
        List<Medication> medications = medicationService.getAllMedications();

        // Assert
        assertNotNull(medications);
        assertEquals(2, medications.size());
        assertEquals("Paracetamol", medications.get(0).getName());
        verify(medicationRepository, times(1)).findAll();
    }

    @Test
    void testGetMedicationById_Success() {
        // Arrange
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(paracetamol));

        // Act
        Medication result = medicationService.getMedicationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Paracetamol", result.getName());
        assertEquals("Analgesic", result.getDrugClass());
        verify(medicationRepository, times(1)).findById(1L);
    }

    @Test
    void testGetMedicationById_NotFound() {
        // Arrange
        when(medicationRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            medicationService.getMedicationById(99L);
        });

        assertEquals("Medication not found with id: 99", exception.getMessage());
        verify(medicationRepository, times(1)).findById(99L);
    }

    @Test
    void testGetMedicationsByDrugClass() {
        // Arrange
        when(medicationRepository.findByDrugClass("Analgesic")).thenReturn(Arrays.asList(paracetamol));

        // Act
        List<Medication> medications = medicationService.getMedicationsByDrugClass("Analgesic");

        // Assert
        assertNotNull(medications);
        assertEquals(1, medications.size());
        assertEquals("Paracetamol", medications.get(0).getName());
        verify(medicationRepository, times(1)).findByDrugClass("Analgesic");
    }

    @Test
    void testSearchMedications() {
        // Arrange
        when(medicationRepository.searchMedications("paracetamol")).thenReturn(Arrays.asList(paracetamol));

        // Act
        List<Medication> medications = medicationService.searchMedications("paracetamol");

        // Assert
        assertNotNull(medications);
        assertEquals(1, medications.size());
        assertEquals("Paracetamol", medications.get(0).getName());
        verify(medicationRepository, times(1)).searchMedications("paracetamol");
    }

    @Test
    void testGetMedicationsByPrescriptionRequired() {
        // Arrange
        when(medicationRepository.findByPrescriptionRequired(false)).thenReturn(Arrays.asList(paracetamol, ibuprofen));

        // Act
        List<Medication> medications = medicationService.getMedicationsByPrescriptionRequired(false);

        // Assert
        assertNotNull(medications);
        assertEquals(2, medications.size());

        // FIX: Use getPrescriptionRequired() instead of isPrescriptionRequired()
        assertFalse(medications.get(0).getPrescriptionRequired());
        verify(medicationRepository, times(1)).findByPrescriptionRequired(false);
    }

    @Test
    void testSaveMedication() {
        // Arrange
        when(medicationRepository.save(any(Medication.class))).thenReturn(paracetamol);

        // Act
        Medication savedMedication = medicationService.saveMedication(paracetamol);

        // Assert
        assertNotNull(savedMedication);
        assertEquals("Paracetamol", savedMedication.getName());
        verify(medicationRepository, times(1)).save(paracetamol);
    }

    @Test
    void testUpdateMedication() {
        // Arrange
        Medication updatedMedication = new Medication();
        updatedMedication.setName("Updated Paracetamol");
        updatedMedication.setGenericName("Updated Acetaminophen");
        updatedMedication.setDrugClass("Updated Analgesic");
        updatedMedication.setCommonUses("Updated uses");
        updatedMedication.setDosageForms("Updated forms");
        updatedMedication.setUsageInstructions("Updated instructions");
        updatedMedication.setPrecautions("Updated precautions");
        updatedMedication.setSideEffects("Updated side effects");
        updatedMedication.setStorageInstructions("Updated storage");
        updatedMedication.setPrescriptionRequired(true); // Changed to true

        when(medicationRepository.findById(1L)).thenReturn(Optional.of(paracetamol));
        when(medicationRepository.save(any(Medication.class))).thenReturn(paracetamol);

        // Act
        Medication result = medicationService.updateMedication(1L, updatedMedication);

        // Assert
        assertNotNull(result);
        verify(medicationRepository, times(1)).findById(1L);
        verify(medicationRepository, times(1)).save(any(Medication.class));
    }

    @Test
    void testDeleteMedication() {
        // Arrange
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(paracetamol));
        doNothing().when(medicationRepository).delete(paracetamol);

        // Act
        medicationService.deleteMedication(1L);

        // Assert
        verify(medicationRepository, times(1)).findById(1L);
        verify(medicationRepository, times(1)).delete(paracetamol);
    }

    @Test
    void testCountMedications() {
        // Arrange
        when(medicationRepository.count()).thenReturn(10L);

        // Act
        long count = medicationService.countMedications();

        // Assert
        assertEquals(10L, count);
        verify(medicationRepository, times(1)).count();
    }

    @Test
    void testGetAllDrugClasses() {
        // Arrange
        List<String> drugClasses = Arrays.asList("Analgesic", "NSAID", "Antibiotic");
        when(medicationRepository.findDistinctDrugClasses()).thenReturn(drugClasses);

        // Act
        List<String> result = medicationService.getAllDrugClasses();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Analgesic", result.get(0));
        verify(medicationRepository, times(1)).findDistinctDrugClasses();
    }
}