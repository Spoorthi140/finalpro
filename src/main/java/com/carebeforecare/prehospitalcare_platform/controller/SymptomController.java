package com.carebeforecare.prehospitalcare_platform.controller;

import com.carebeforecare.prehospitalcare_platform.model.entity.Symptom;
import com.carebeforecare.prehospitalcare_platform.service.SymptomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/symptoms")
public class SymptomController {

    private final SymptomService symptomService;

    public SymptomController(SymptomService symptomService) {
        this.symptomService = symptomService;
    }

    @GetMapping
    public String getAllSymptoms(Model model) {
        System.out.println("🩺 Fetching all symptoms");
        List<Symptom> symptoms = symptomService.getAllSymptoms();
        model.addAttribute("symptoms", symptoms);
        model.addAttribute("pageTitle", "Symptoms Guide");
        System.out.println("✅ Loaded " + symptoms.size() + " symptoms");
        return "symptoms";
    }

    @GetMapping("/{id}")
    public String getSymptomDetail(@PathVariable Long id, Model model) {
        System.out.println("🔍 Fetching symptom with ID: " + id);
        Optional<Symptom> symptomOptional = symptomService.getSymptomById(id);

        if (symptomOptional.isPresent()) {
            Symptom symptom = symptomOptional.get();
            model.addAttribute("symptom", symptom);
            model.addAttribute("pageTitle", symptom.getName() + " - Details");
            System.out.println("✅ Loaded symptom: " + symptom.getName());
            return "symptom-detail";
        } else {
            model.addAttribute("error", "Symptom not found");
            return "redirect:/symptoms";
        }
    }
}