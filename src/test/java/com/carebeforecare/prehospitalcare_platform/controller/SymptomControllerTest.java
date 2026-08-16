package com.carebeforecare.prehospitalcare_platform.controller;

import com.carebeforecare.prehospitalcare_platform.model.entity.Symptom;
import com.carebeforecare.prehospitalcare_platform.service.SymptomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SymptomControllerTest {

    @Mock
    private SymptomService symptomService;

    @InjectMocks
    private SymptomController symptomController;

    private MockMvc mockMvc;

    private Symptom fever;
    private Symptom cough;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(symptomController)
                .setViewResolvers(viewResolver)
                .build();

        fever = new Symptom();
        fever.setId(1L);
        fever.setName("Fever");
        fever.setSeverity("Moderate");

        cough = new Symptom();
        cough.setId(2L);
        cough.setName("Cough");
        cough.setSeverity("Mild");
    }

    @Test
    void testGetAllSymptoms() throws Exception {
        List<Symptom> symptoms = Arrays.asList(fever, cough);
        when(symptomService.getAllSymptoms()).thenReturn(symptoms);

        mockMvc.perform(get("/symptoms"))
                .andExpect(status().isOk())
                .andExpect(view().name("symptoms"))
                .andExpect(model().attributeExists("symptoms"))
                .andExpect(model().attribute("symptoms", symptoms));

        verify(symptomService, times(1)).getAllSymptoms();
    }

    @Test
    void testGetSymptomDetail_Found() throws Exception {
        when(symptomService.getSymptomById(1L)).thenReturn(Optional.of(fever));

        mockMvc.perform(get("/symptoms/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("symptom-detail"))
                .andExpect(model().attributeExists("symptom"))
                .andExpect(model().attribute("symptom", fever));

        verify(symptomService, times(1)).getSymptomById(1L);
    }

    @Test
    void testGetSymptomDetail_NotFound() throws Exception {
        when(symptomService.getSymptomById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/symptoms/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/symptoms"));

        verify(symptomService, times(1)).getSymptomById(999L);
    }
}
