package com.example.erp.app.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ErpIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void grade_can_be_created_and_queried() throws Exception {
        mockMvc.perform(post("/api/v1/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":\"s-1\",\"courseName\":\"math\",\"score\":88}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value("s-1"))
                .andExpect(jsonPath("$.score").value(88.0));
    }

    @Test
    void attendance_clock_in_uses_cross_domain_api() throws Exception {
        mockMvc.perform(post("/api/v1/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":\"s-2\",\"courseName\":\"math\",\"score\":70}"))
                .andExpect(status().isOk());

        String response = mockMvc.perform(post("/api/v1/attendance/clock-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\":\"s-2\",\"clockInTime\":\"2026-09-16T08:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PRESENT"))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/api/v1/attendance/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value("s-2"));
    }

    @Test
    void attendance_not_found_returns_404() throws Exception {
        mockMvc.perform(get("/api/v1/attendance/{id}", "missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void openapi_end_is_exposed_on_the_boot_assembly() throws Exception {
        mockMvc.perform(get("/openapi/v1/attendance").param("studentId", "s-3"))
                .andExpect(status().isOk());
    }
}