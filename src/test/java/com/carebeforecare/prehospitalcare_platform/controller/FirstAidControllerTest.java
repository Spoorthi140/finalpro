package com.carebeforecare.prehospitalcare_platform.controller;

import com.carebeforecare.prehospitalcare_platform.model.entity.FirstAidGuide;
import com.carebeforecare.prehospitalcare_platform.service.FirstAidService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FirstAidControllerTest {

    @Mock
    private FirstAidService firstAidService;

    @InjectMocks
    private FirstAidController firstAidController;

    private MockMvc mockMvc;

    private FirstAidGuide cprGuide;
    private FirstAidGuide chokingGuide;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(firstAidController).build();

        cprGuide = new FirstAidGuide();
        cprGuide.setId(1L);
        cprGuide.setTitle("CPR Guide");
        cprGuide.setDescription("Cardiopulmonary resuscitation");
        cprGuide.setCategory("CARDIAC");
        cprGuide.setDifficulty("ADVANCED");

        chokingGuide = new FirstAidGuide();
        chokingGuide.setId(2L);
        chokingGuide.setTitle("Choking Guide");
        chokingGuide.setDescription("Help for choking");
        chokingGuide.setCategory("RESPIRATORY");
        chokingGuide.setDifficulty("INTERMEDIATE");
    }

    @Test
    void testListFirstAidGuides() throws Exception {
        // Arrange
        List<FirstAidGuide> guides = Arrays.asList(cprGuide, chokingGuide);
        List<String> categories = Arrays.asList("CARDIAC", "RESPIRATORY");

        when(firstAidService.getAllFirstAidGuides()).thenReturn(guides);
        when(firstAidService.getAllCategories()).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/firstaid"))
                .andExpect(status().isOk())
                .andExpect(view().name("firstaid/list"))
                .andExpect(model().attributeExists("guides"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attribute("guides", guides))
                .andExpect(model().attribute("categories", categories));

        verify(firstAidService, times(1)).getAllFirstAidGuides();
        verify(firstAidService, times(1)).getAllCategories();
    }

    @Test
    void testGetFirstAidGuideDetail_Found() throws Exception {
        // Arrange
        List<FirstAidGuide> relatedGuides = Arrays.asList(chokingGuide);

        when(firstAidService.getFirstAidGuideById(1L)).thenReturn(Optional.of(cprGuide));
        when(firstAidService.getRelatedFirstAidGuides("CARDIAC", 1L)).thenReturn(relatedGuides);

        // Act & Assert
        mockMvc.perform(get("/firstaid/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("firstaid/detail"))
                .andExpect(model().attributeExists("guide"))
                .andExpect(model().attributeExists("relatedGuides"))
                .andExpect(model().attribute("guide", cprGuide))
                .andExpect(model().attribute("relatedGuides", relatedGuides));

        verify(firstAidService, times(1)).getFirstAidGuideById(1L);
        verify(firstAidService, times(1)).getRelatedFirstAidGuides("CARDIAC", 1L);
    }

    @Test
    void testGetFirstAidGuideDetail_NotFound() throws Exception {
        // Arrange
        when(firstAidService.getFirstAidGuideById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/firstaid/99"))
                .andExpect(status().isOk())
                .andExpect(view().name("firstaid/detail"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "First aid guide not found"));

        verify(firstAidService, times(1)).getFirstAidGuideById(99L);
        verify(firstAidService, never()).getRelatedFirstAidGuides(anyString(), anyLong());
    }

    @Test
    void testSearchFirstAidGuides() throws Exception {
        // Arrange
        List<FirstAidGuide> guides = Arrays.asList(cprGuide);
        List<String> categories = Arrays.asList("CARDIAC", "RESPIRATORY");

        when(firstAidService.searchFirstAidGuides("cpr")).thenReturn(guides);
        when(firstAidService.getAllCategories()).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/firstaid/search")
                .param("keyword", "cpr"))
                .andExpect(status().isOk())
                .andExpect(view().name("firstaid/list"))
                .andExpect(model().attributeExists("guides"))
                .andExpect(model().attributeExists("searchKeyword"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attribute("guides", guides))
                .andExpect(model().attribute("searchKeyword", "cpr"))
                .andExpect(model().attribute("categories", categories));

        verify(firstAidService, times(1)).searchFirstAidGuides("cpr");
        verify(firstAidService, times(1)).getAllCategories();
    }

    @Test
    void testFilterByCategory() throws Exception {
        // Arrange
        List<FirstAidGuide> guides = Arrays.asList(cprGuide);
        List<String> categories = Arrays.asList("CARDIAC", "RESPIRATORY");

        when(firstAidService.getFirstAidGuidesByCategory("CARDIAC")).thenReturn(guides);
        when(firstAidService.getAllCategories()).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/firstaid/category/CARDIAC"))
                .andExpect(status().isOk())
                .andExpect(view().name("firstaid/list"))
                .andExpect(model().attributeExists("guides"))
                .andExpect(model().attributeExists("selectedCategory"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attribute("guides", guides))
                .andExpect(model().attribute("selectedCategory", "CARDIAC"))
                .andExpect(model().attribute("categories", categories));

        verify(firstAidService, times(1)).getFirstAidGuidesByCategory("CARDIAC");
        verify(firstAidService, times(1)).getAllCategories();
    }

    @Test
    void testHandleUnknownRoutes() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/firstaid/unknown"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/firstaid"));
    }

    @Test
    void testListFirstAidGuides_ExceptionHandling() throws Exception {
        // Arrange
        when(firstAidService.getAllFirstAidGuides()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/firstaid"))
                .andExpect(status().isOk())
                .andExpect(view().name("firstaid/list"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Unable to load first aid guides. Please try again."));

        verify(firstAidService, times(1)).getAllFirstAidGuides();
    }

    @Test
    void testSearchFirstAidGuides_ExceptionHandling() throws Exception {
        // Arrange
        when(firstAidService.searchFirstAidGuides("cpr")).thenThrow(new RuntimeException("Search error"));

        // Act & Assert
        mockMvc.perform(get("/firstaid/search")
                .param("keyword", "cpr"))
                .andExpect(status().isOk())
                .andExpect(view().name("firstaid/list"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Unable to search first aid guides. Please try again."));

        verify(firstAidService, times(1)).searchFirstAidGuides("cpr");
    }
}