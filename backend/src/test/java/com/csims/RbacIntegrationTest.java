package com.csims;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RbacIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("User with USER_VIEW authority can access /api/users")
    @WithMockUser(authorities = {"USER_VIEW"})
    void userWithPermissionAllowed() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("User lacking USER_VIEW authority is rejected with 403 Forbidden")
    @WithMockUser(authorities = {"WORKER_ONLY"})
    void userWithoutPermissionForbidden() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User without PROJECT_CREATE cannot create a project (403 Forbidden)")
    @WithMockUser(authorities = {"PROJECT_VIEW"})
    void unauthorizedProjectCreationForbidden() throws Exception {
        Map<String, Object> projectData = Map.of(
                "name", "Unauthorized Sky Tower",
                "status", "PLANNED"
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectData)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User without AUDIT_VIEW authority cannot view audit trail (403 Forbidden)")
    @WithMockUser(authorities = {"PROJECT_VIEW", "TASK_VIEW"})
    void unauthorizedAuditAccessForbidden() throws Exception {
        mockMvc.perform(get("/api/audit-logs"))
                .andExpect(status().isForbidden());
    }
}
