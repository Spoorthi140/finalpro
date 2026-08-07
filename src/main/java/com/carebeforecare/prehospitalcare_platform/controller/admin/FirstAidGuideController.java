package com.carebeforecare.prehospitalcare_platform.controller.admin;

import com.carebeforecare.prehospitalcare_platform.model.entity.FirstAidGuide;
import com.carebeforecare.prehospitalcare_platform.repository.FirstAidGuideRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/first-aid-guides")
public class FirstAidGuideController {

    private final FirstAidGuideRepository firstAidGuideRepository;

    public FirstAidGuideController(FirstAidGuideRepository firstAidGuideRepository) {
        this.firstAidGuideRepository = firstAidGuideRepository;
    }

    // List all first aid guides
    @GetMapping
    public String listFirstAidGuides(Model model) {
        List<FirstAidGuide> guides = firstAidGuideRepository.findAll();

        // Calculate statistics
        long totalGuides = guides.size();
        long lowSeverityCount = guides.stream()
                .filter(g -> g.getSeverity().equalsIgnoreCase("LOW") || g.getSeverity().equalsIgnoreCase("Mild"))
                .count();
        long mediumSeverityCount = guides.stream()
                .filter(g -> g.getSeverity().equalsIgnoreCase("MEDIUM") || g.getSeverity().equalsIgnoreCase("Moderate"))
                .count();
        long highSeverityCount = guides.stream()
                .filter(g -> g.getSeverity().equalsIgnoreCase("HIGH") || g.getSeverity().equalsIgnoreCase("Critical"))
                .count();

        model.addAttribute("pageTitle", "Manage First Aid Guides");
        model.addAttribute("guides", guides);
        model.addAttribute("totalGuides", totalGuides);
        model.addAttribute("lowSeverityCount", lowSeverityCount);
        model.addAttribute("mediumSeverityCount", mediumSeverityCount);
        model.addAttribute("highSeverityCount", highSeverityCount);

        return "admin/first-aid-guides/list";
    }

    // Show create form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("pageTitle", "Create First Aid Guide");
        model.addAttribute("guide", new FirstAidGuide());
        return "admin/first-aid-guides/form";
    }

    // Show edit form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<FirstAidGuide> guideOptional = firstAidGuideRepository.findById(id);

        if (guideOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "First Aid Guide not found");
            return "redirect:/admin/first-aid-guides";
        }

        model.addAttribute("pageTitle", "Edit First Aid Guide");
        model.addAttribute("guide", guideOptional.get());
        return "admin/first-aid-guides/form";
    }

    // Save or update first aid guide
    @PostMapping("/save")
    public String saveFirstAidGuide(@ModelAttribute FirstAidGuide guide, RedirectAttributes redirectAttributes) {
        try {
            // Set timestamps
            if (guide.getId() == null) {
                guide.setCreatedAt(LocalDateTime.now());
            }
            guide.setUpdatedAt(LocalDateTime.now());

            // Set default values if null
            if (guide.getPriority() == null) {
                guide.setPriority("Standard");
            }
            if (guide.getIcon() == null) {
                guide.setIcon("first-aid");
            }

            firstAidGuideRepository.save(guide);

            String message = guide.getId() != null ?
                "First Aid Guide updated successfully!" :
                "First Aid Guide created successfully!";

            redirectAttributes.addFlashAttribute("success", message);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving First Aid Guide: " + e.getMessage());
        }

        return "redirect:/admin/first-aid-guides";
    }

    // Delete first aid guide
    @GetMapping("/delete/{id}")
    public String deleteFirstAidGuide(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<FirstAidGuide> guideOptional = firstAidGuideRepository.findById(id);

            if (guideOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "First Aid Guide not found");
            } else {
                firstAidGuideRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "First Aid Guide deleted successfully!");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting First Aid Guide: " + e.getMessage());
        }

        return "redirect:/admin/first-aid-guides";
    }
}