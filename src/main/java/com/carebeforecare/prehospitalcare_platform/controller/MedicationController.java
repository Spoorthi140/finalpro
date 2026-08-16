package com.carebeforecare.prehospitalcare_platform.controller;

import com.carebeforecare.prehospitalcare_platform.model.entity.Medication;
import com.carebeforecare.prehospitalcare_platform.service.MedicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/medications")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping
    public String getAllMedications(Model model) {
        System.out.println("💊 Fetching all medications");
        List<Medication> medications = medicationService.getAllMedications();
        model.addAttribute("medications", medications);
        model.addAttribute("pageTitle", "Medications Guide");
        System.out.println("✅ Loaded " + medications.size() + " medications");
        return "medications";
    }

    @GetMapping("/{id}")
    public String getMedicationDetail(@PathVariable Long id, Model model) {
        System.out.println("🔍 Fetching medication with ID: " + id);
        try {
            Medication medication = medicationService.getMedicationById(id);
            model.addAttribute("medication", medication);
            model.addAttribute("pageTitle", medication.getName() + " - Medication Details");
            System.out.println("✅ Loaded medication: " + medication.getName());
            return "medication-detail";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading medication details");
            return "redirect:/medications";
        }
    }

    @GetMapping("/class/{drugClass}")
    public String getMedicationsByDrugClass(@PathVariable String drugClass, Model model) {
        System.out.println("📂 Fetching medications by class: " + drugClass);
        List<Medication> medications = medicationService.getMedicationsByDrugClass(drugClass);
        model.addAttribute("medications", medications);
        model.addAttribute("drugClass", drugClass);
        model.addAttribute("pageTitle", drugClass + " Medications");
        return "medications";
    }
}