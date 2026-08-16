package com.carebeforecare.prehospitalcare_platform.controller.admin;

import com.carebeforecare.prehospitalcare_platform.model.entity.Medication;
import com.carebeforecare.prehospitalcare_platform.service.MedicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/medications")
public class AdminMedicationController {

    private final MedicationService medicationService;

    public AdminMedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping
    public String listMedications(Model model) {
        model.addAttribute("medications", medicationService.getAllMedications());
        model.addAttribute("pageTitle", "Manage Medications");
        return "admin/medications/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("medication", new Medication());
        model.addAttribute("pageTitle", "Create Medication");
        return "admin/medications/form";
    }

    @PostMapping("/save")
    public String saveMedication(@ModelAttribute Medication medication, RedirectAttributes redirectAttributes) {
        try {
            if (medication.getId() == null) {
                medication.setCreatedAt(LocalDateTime.now());
            }
            medication.setUpdatedAt(LocalDateTime.now());

            medicationService.saveMedication(medication);
            redirectAttributes.addFlashAttribute("success", "Medication saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving medication: " + e.getMessage());
        }
        return "redirect:/admin/medications";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Medication medication = medicationService.getMedicationById(id);
            model.addAttribute("medication", medication);
            model.addAttribute("pageTitle", "Edit Medication");
            return "admin/medications/form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Medication not found!");
            return "redirect:/admin/medications";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteMedication(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            medicationService.deleteMedication(id);
            redirectAttributes.addFlashAttribute("success", "Medication deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting medication: " + e.getMessage());
        }
        return "redirect:/admin/medications";
    }
}