package com.carebeforecare.prehospitalcare_platform.controller.admin;

import com.carebeforecare.prehospitalcare_platform.service.FirstAidService;
import com.carebeforecare.prehospitalcare_platform.service.SymptomService;
import com.carebeforecare.prehospitalcare_platform.service.MedicationService;
import com.carebeforecare.prehospitalcare_platform.service.HealthAssessmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final FirstAidService firstAidService;
    private final SymptomService symptomService;
    private final MedicationService medicationService;
    private final HealthAssessmentService healthAssessmentService;

    public AdminController(FirstAidService firstAidService,
                          SymptomService symptomService,
                          MedicationService medicationService,
                          HealthAssessmentService healthAssessmentService) {
        this.firstAidService = firstAidService;
        this.symptomService = symptomService;
        this.medicationService = medicationService;
        this.healthAssessmentService = healthAssessmentService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        model.addAttribute("pageTitle", "Admin Login");
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long firstAidCount = firstAidService.countFirstAidGuides();
        long symptomsCount = symptomService.countSymptoms();
        long medicationsCount = medicationService.countMedications();

        long totalAssessments = healthAssessmentService.countAssessments();
        long lowRiskCount = healthAssessmentService.countByRiskLevel("LOW RISK");
        long moderateRiskCount = healthAssessmentService.countByRiskLevel("MODERATE RISK");
        long highRiskCount = healthAssessmentService.countByRiskLevel("HIGH RISK");

        model.addAttribute("pageTitle", "Admin Dashboard");
        model.addAttribute("firstAidCount", firstAidCount);
        model.addAttribute("symptomsCount", symptomsCount);
        model.addAttribute("medicationsCount", medicationsCount);

        model.addAttribute("totalAssessments", totalAssessments);
        model.addAttribute("lowRiskCount", lowRiskCount);
        model.addAttribute("moderateRiskCount", moderateRiskCount);
        model.addAttribute("highRiskCount", highRiskCount);
        model.addAttribute("recentAssessments", healthAssessmentService.getRecentAssessments(5));

        return "admin/dashboard";
    }
}
