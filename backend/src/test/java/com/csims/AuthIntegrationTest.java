package com.csims;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Unauthenticated request to protected endpoint returns 401")
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Valid admin login returns 200, session cookie, and user details")
    void validAdminLoginSucceeds() throws Exception {
        Map<String, String> creds = Map.of("username", "admin", "password", "Admin@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.roleName").value("Administrator"));
    }

    @Test
    @DisplayName("Valid Project Manager login returns 200")
    void validProjectManagerLoginSucceeds() throws Exception {
        Map<String, String> creds = Map.of("username", "pm_sarah", "password", "Pm@12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roleName").value("Project Manager"));
    }

    @Test
    @DisplayName("Valid Site Engineer login returns 200")
    void validSiteEngineerLoginSucceeds() throws Exception {
        Map<String, String> creds = Map.of("username", "site_eng", "password", "Eng@12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roleName").value("Site Engineer"));
    }

    @Test
    @DisplayName("Valid Safety Officer login returns 200")
    void validSafetyOfficerLoginSucceeds() throws Exception {
        Map<String, String> creds = Map.of("username", "safety_dan", "password", "Safe@12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roleName").value("Safety Officer"));
    }

    @Test
    @DisplayName("Valid Inventory Manager login returns 200")
    void validInventoryManagerLoginSucceeds() throws Exception {
        Map<String, String> creds = Map.of("username", "inv_maria", "password", "Inv@12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roleName").value("Inventory Manager"));
    }

    @Test
    @DisplayName("Valid Equipment Manager login returns 200")
    void validEquipmentManagerLoginSucceeds() throws Exception {
        Map<String, String> creds = Map.of("username", "equip_sam", "password", "Equip@12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roleName").value("Equipment Manager"));
    }

    @Test
    @DisplayName("Valid Contractor login returns 200")
    void validContractorLoginSucceeds() throws Exception {
        Map<String, String> creds = Map.of("username", "contractor_apex", "password", "Cont@12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roleName").value("Contractor"));
    }

    @Test
    @DisplayName("Valid Worker login returns 200")
    void validWorkerLoginSucceeds() throws Exception {
        Map<String, String> creds = Map.of("username", "worker_bill", "password", "Worker@12345");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roleName").value("Worker"));
    }

    @Test
    @DisplayName("Invalid password returns 401 with generic error")
    void invalidPasswordReturns401() throws Exception {
        Map<String, String> creds = Map.of("username", "admin", "password", "InvalidPassword123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Invalid username or password")));
    }
}
