package com.carebeforecare.prehospitalcare_platform.controller;

import com.carebeforecare.prehospitalcare_platform.service.SymptomService;
import com.carebeforecare.prehospitalcare_platform.service.FirstAidService;
import com.carebeforecare.prehospitalcare_platform.service.MedicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final SymptomService symptomService;
    private final FirstAidService firstAidService;
    private final MedicationService medicationService;

    public HomeController(SymptomService symptomService,
                         FirstAidService firstAidService,
                         MedicationService medicationService) {
        this.symptomService = symptomService;
        this.firstAidService = firstAidService;
        this.medicationService = medicationService;
    }

    @GetMapping("/")
    public String home(Model model) {
        System.out.println("🏠 Loading home page");

        long symptomCount = symptomService.getAllSymptoms().size();
        long guideCount = firstAidService.getAllFirstAidGuides().size();
        long medicationCount = medicationService.getAllMedications().size();

        model.addAttribute("pageTitle", "Pre-Hospital Care Platform");
        model.addAttribute("welcomeMessage", "Your trusted guide for symptom information and first aid");
        model.addAttribute("symptomCount", symptomCount);
        model.addAttribute("guideCount", guideCount);
        model.addAttribute("medicationCount", medicationCount);

        System.out.println("📊 Stats - Symptoms: " + symptomCount + ", Guides: " + guideCount + ", Medications: " + medicationCount);
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About Pre-Hospital Care Platform");
        return "about";
    }
}