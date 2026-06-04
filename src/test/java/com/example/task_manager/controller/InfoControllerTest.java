package com.example.task_manager.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Profile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.task_manager.component.InfoService;


@WebMvcTest(InfoController.class)
@WithMockUser
class InfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InfoService infoService;

    @Test
    void getVersion_shouldBePublicEndpoint() throws Exception {

        when(infoService.getVersion()).thenReturn("0.0.1");

        mockMvc.perform(get("/info/version"))
                .andExpect(status().isOk());
    }
}