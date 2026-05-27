package com.example.task_manager.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.task_manager.dto.UserProjectTaskReport;
import com.example.task_manager.model.Role;
import com.example.task_manager.model.User;
import com.example.task_manager.security.CustomUserDetails;
import com.example.task_manager.security.SecurityConfig;
import com.example.task_manager.security.UserSecurityService;
import com.example.task_manager.service.TaskService;

@WebMvcTest(AdminController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc()
@Import(SecurityConfig.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private UserSecurityService userSecurityService;


    LocalDateTime currDate = LocalDateTime.now();

    private void authenticate(User user) {
        CustomUserDetails principal = new CustomUserDetails(user);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
            new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
            )
        );

        SecurityContextHolder.setContext(context);

    }

    private User admin(Long id) {
        User u = new User("admin" + id, "admin" + id + "@test.ru");
        u.setId(id);
        u.setRole(Role.ADMIN);
        return u;
    }

        private User user(Long id) {
        User u = new User("user" + id, "user" + id + "@test.ru");
        u.setId(id);
        u.setRole(Role.USER);
        return u;
    }

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
    }

   @Test
    void getReport_admin_shouldReturnReport() throws Exception {

        authenticate(admin(1L));

        UserProjectTaskReport report =
                new UserProjectTaskReport(1L, 1L, 5L);

        when(taskService.getReport(null, Role.ADMIN))
                .thenReturn(List.of(report));

        mockMvc.perform(get("/admin/report/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].projectId").value(1))
                .andExpect(jsonPath("$[0].taskCount").value(5));

        verify(taskService).getReport(null, Role.ADMIN);
    }

    @Test
    void getReport_user_shouldReturnForbidden() throws Exception {

       authenticate(user(1L)); 

        mockMvc.perform(get("/admin/report/tasks"))
                .andExpect(status().isForbidden());

    }

    @Test
    void getReport_unauthorized_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(get("/admin/report/tasks"))
                .andExpect(status().isUnauthorized());

    }


    
}
