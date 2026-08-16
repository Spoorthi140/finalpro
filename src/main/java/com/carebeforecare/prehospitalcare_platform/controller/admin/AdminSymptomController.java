package com.carebeforecare.prehospitalcare_platform.controller.admin;

import com.carebeforecare.prehospitalcare_platform.model.entity.Symptom;
import com.carebeforecare.prehospitalcare_platform.service.SymptomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/admin/symptoms")
public class AdminSymptomController {

    private final SymptomService symptomService;

    public AdminSymptomController(SymptomService symptomService) {
        this.symptomService = symptomService;
    }

    @GetMapping
    public String listSymptoms(Model model) {
        model.addAttribute("symptoms", symptomService.getAllSymptoms());
        model.addAttribute("pageTitle", "Manage Symptoms");
        return "admin/symptoms/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("symptom", new Symptom());
        model.addAttribute("pageTitle", "Create Symptom");
        return "admin/symptoms/form";
    }

    @PostMapping("/save")
    public String saveSymptom(@ModelAttribute Symptom symptom, RedirectAttributes redirectAttributes) {
        try {
            if (symptom.getId() == null) {
                symptom.setCreatedAt(LocalDateTime.now());
            }
            symptom.setUpdatedAt(LocalDateTime.now());

            symptomService.saveSymptom(symptom);
            redirectAttributes.addFlashAttribute("success", "Symptom saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving symptom: " + e.getMessage());
        }
        return "redirect:/admin/symptoms";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Symptom> symptom = symptomService.getSymptomById(id);
        if (symptom.isPresent()) {
            model.addAttribute("symptom", symptom.get());
            model.addAttribute("pageTitle", "Edit Symptom");
            return "admin/symptoms/form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Symptom not found!");
            return "redirect:/admin/symptoms";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteSymptom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            symptomService.deleteSymptom(id);
            redirectAttributes.addFlashAttribute("success", "Symptom deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting symptom: " + e.getMessage());
        }
        return "redirect:/admin/symptoms";
    }
}