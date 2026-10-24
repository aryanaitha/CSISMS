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
    @DisplayName("Admin / User with USER_VIEW authority can access /api/users")
    @WithMockUser(authorities = {"USER_VIEW"})
    void adminWithPermissionAllowed() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Project Manager is denied access to Audit Logs (AUDIT_VIEW - 403 Forbidden)")
    @WithMockUser(authorities = {"PROJECT_VIEW", "PROJECT_CREATE", "WORKFORCE_VIEW", "TASK_VIEW"})
    void projectManagerDeniedAuditLogs() throws Exception {
        mockMvc.perform(get("/api/audit-logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Site Engineer is denied access to Create Projects (PROJECT_CREATE - 403 Forbidden)")
    @WithMockUser(authorities = {"PROJECT_VIEW", "WORKFORCE_VIEW", "TASK_VIEW", "TASK_UPDATE", "ATTENDANCE_RECORD"})
    void siteEngineerDeniedProjectCreation() throws Exception {
        Map<String, Object> projectData = Map.of(
                "projectName", "Unauthorized Bridge Project",
                "status", "Planning"
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectData)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Safety Officer is denied access to Issue Inventory (INVENTORY_ISSUE - 403 Forbidden)")
    @WithMockUser(authorities = {"PROJECT_VIEW", "SAFETY_INSPECTION_CREATE", "SAFETY_VIOLATION_CREATE", "INCIDENT_VIEW"})
    void safetyOfficerDeniedInventoryIssue() throws Exception {
        Map<String, Object> movement = Map.of(
                "materialId", 1,
                "quantity", 10,
                "projectId", 1
        );

        mockMvc.perform(post("/api/inventory/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movement)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Inventory Manager is denied access to Create Projects (PROJECT_CREATE - 403 Forbidden)")
    @WithMockUser(authorities = {"PROJECT_VIEW", "INVENTORY_VIEW", "INVENTORY_RECEIPT", "INVENTORY_ISSUE"})
    void inventoryManagerDeniedProjectCreation() throws Exception {
        Map<String, Object> projectData = Map.of(
                "projectName", "Unauthorized Warehouse Project",
                "status", "Planning"
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectData)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Equipment Manager is denied access to Create Workers (WORKFORCE_CREATE - 403 Forbidden)")
    @WithMockUser(authorities = {"PROJECT_VIEW", "EQUIPMENT_VIEW", "EQUIPMENT_CREATE", "MAINTENANCE_SCHEDULE"})
    void equipmentManagerDeniedWorkerCreation() throws Exception {
        Map<String, Object> workerData = Map.of(
                "fullName", "Unauthorized Worker",
                "trade", "Welder"
        );

        mockMvc.perform(post("/api/workforce/workers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(workerData)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Contractor is denied access to User Management (USER_VIEW - 403 Forbidden)")
    @WithMockUser(authorities = {"PROJECT_VIEW", "WORKFORCE_VIEW", "TASK_VIEW", "TASK_UPDATE"})
    void contractorDeniedUserManagement() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Worker is denied access to User Management (USER_VIEW - 403 Forbidden)")
    @WithMockUser(authorities = {"TASK_VIEW", "TASK_UPDATE", "ATTENDANCE_VIEW"})
    void workerDeniedUserManagement() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Worker is denied access to Create Projects (PROJECT_CREATE - 403 Forbidden)")
    @WithMockUser(authorities = {"TASK_VIEW", "TASK_UPDATE", "ATTENDANCE_VIEW"})
    void workerDeniedProjectCreation() throws Exception {
        Map<String, Object> projectData = Map.of(
                "projectName", "Unauthorized Tower Project",
                "status", "Planning"
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectData)))
                .andExpect(status().isForbidden());
    }
}
