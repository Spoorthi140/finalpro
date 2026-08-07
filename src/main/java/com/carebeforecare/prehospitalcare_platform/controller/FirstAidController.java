package com.carebeforecare.prehospitalcare_platform.controller;

import com.carebeforecare.prehospitalcare_platform.model.entity.FirstAidGuide;
import com.carebeforecare.prehospitalcare_platform.service.FirstAidService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/firstaid")
public class FirstAidController {

    private final FirstAidService firstAidService;

    public FirstAidController(FirstAidService firstAidService) {
        this.firstAidService = firstAidService;
    }

    @GetMapping
    public String listFirstAidGuides(Model model) {
        try {
            System.out.println("📚 Fetching all first aid guides");
            List<FirstAidGuide> guides = firstAidService.getAllFirstAidGuides();
            List<String> categories = firstAidService.getAllCategories();

            model.addAttribute("guides", guides);
            model.addAttribute("categories", categories);
            model.addAttribute("pageTitle", "First Aid Guides");

            System.out.println("✅ Loaded " + guides.size() + " first aid guides");
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load first aid guides. Please try again.");
            model.addAttribute("guides", Collections.emptyList());
            model.addAttribute("categories", Collections.emptyList());
            model.addAttribute("pageTitle", "First Aid Guides - Error");
        }
        return "firstaid/list";
    }

    @GetMapping("/{id}")
    public String getFirstAidGuideDetail(@PathVariable Long id, Model model) {
        System.out.println("🔍 Fetching first aid guide with ID: " + id);
        Optional<FirstAidGuide> guideOptional = firstAidService.getFirstAidGuideById(id);

        if (guideOptional.isPresent()) {
            FirstAidGuide guide = guideOptional.get();
            model.addAttribute("guide", guide);
            model.addAttribute("pageTitle", guide.getTitle());

            List<FirstAidGuide> relatedGuides = firstAidService.getRelatedFirstAidGuides(guide.getCategory(), id);
            model.addAttribute("relatedGuides", relatedGuides);

            System.out.println("✅ Loaded guide: " + guide.getTitle());
            return "firstaid/detail";
        } else {
            model.addAttribute("error", "First aid guide not found");
            return "firstaid/detail";
        }
    }

    @GetMapping("/search")
    public String searchFirstAidGuides(@RequestParam String keyword, Model model) {
        try {
            System.out.println("🔎 Searching guides with keyword: " + keyword);
            List<FirstAidGuide> guides = firstAidService.searchFirstAidGuides(keyword);
            List<String> categories = firstAidService.getAllCategories();

            model.addAttribute("guides", guides);
            model.addAttribute("searchKeyword", keyword);
            model.addAttribute("categories", categories);
            model.addAttribute("pageTitle", "Search: " + keyword);

            System.out.println("✅ Found " + guides.size() + " results for '" + keyword + "'");
        } catch (Exception e) {
            model.addAttribute("error", "Unable to search first aid guides. Please try again.");
            model.addAttribute("guides", Collections.emptyList());
            model.addAttribute("categories", Collections.emptyList());
            model.addAttribute("searchKeyword", keyword);
            model.addAttribute("pageTitle", "Search Error");
        }
        return "firstaid/list";
    }

    @GetMapping("/category/{category}")
    public String filterByCategory(@PathVariable String category, Model model) {
        System.out.println("📂 Filtering guides by category: " + category);
        List<FirstAidGuide> guides = firstAidService.getFirstAidGuidesByCategory(category);
        List<String> categories = firstAidService.getAllCategories();

        model.addAttribute("guides", guides);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", categories);
        model.addAttribute("pageTitle", category + " - First Aid Guides");

        return "firstaid/list";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch() {
        return "redirect:/firstaid";
    }
}
