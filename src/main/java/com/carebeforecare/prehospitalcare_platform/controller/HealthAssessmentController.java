package com.carebeforecare.prehospitalcare_platform.controller;

import com.carebeforecare.prehospitalcare_platform.model.dto.HealthAssessmentDTO;
import com.carebeforecare.prehospitalcare_platform.model.entity.HealthAssessment;
import com.carebeforecare.prehospitalcare_platform.service.HealthAssessmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/assessment")
public class HealthAssessmentController {

    private final HealthAssessmentService service;

    public HealthAssessmentController(HealthAssessmentService service) {
        this.service = service;
    }

    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("assessmentDTO", new HealthAssessmentDTO());
        model.addAttribute("pageTitle", "Health Risk Assessment");
        return "assessment-form";
    }

    @PostMapping
    public String submitForm(@ModelAttribute("assessmentDTO") HealthAssessmentDTO dto, Model model) {
        boolean hasErrors = false;

        // Validation logic
        if (dto.getAge() == null || dto.getAge() <= 0) {
            model.addAttribute("ageError", "Age must be a positive number.");
            hasErrors = true;
        }

        if (dto.getTemperature() == null || dto.getTemperature() < 30.0 || dto.getTemperature() > 45.0) {
            model.addAttribute("temperatureError", "Body temperature must be between 30°C and 45°C.");
            hasErrors = true;
        }

        if (dto.getHeartRate() == null || dto.getHeartRate() < 20 || dto.getHeartRate() > 250) {
            model.addAttribute("heartRateError", "Heart rate must be between 20 BPM and 250 BPM.");
            hasErrors = true;
        }

        if (dto.getGender() == null || dto.getGender().trim().isEmpty()) {
            model.addAttribute("genderError", "Please select your gender.");
            hasErrors = true;
        }

        if (hasErrors) {
            model.addAttribute("pageTitle", "Health Risk Assessment - Try Again");
            return "assessment-form";
        }

        // Clean up fields if other than female is selected
        if (!"Female".equalsIgnoreCase(dto.getGender())) {
            dto.setPregnant(false);
        }

        // Evaluate and save the health assessment
        HealthAssessment result = service.evaluateAndSave(dto);

        model.addAttribute("result", result);
        model.addAttribute("pageTitle", "Assessment Results");
        return "assessment-result";
    }

    @GetMapping("/history")
    public String showHistory(@RequestParam(value = "search", required = false) String search, Model model) {
        List<HealthAssessment> assessments;
        if (search != null && !search.trim().isEmpty()) {
            assessments = service.searchAssessments(search.trim());
            model.addAttribute("searchKeyword", search.trim());
        } else {
            assessments = service.getAllAssessments();
        }

        model.addAttribute("assessments", assessments);
        model.addAttribute("pageTitle", "Assessment History");
        return "assessment-history";
    }
}