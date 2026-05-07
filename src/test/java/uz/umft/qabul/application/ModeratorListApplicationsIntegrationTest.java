package uz.umft.qabul.application;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ModeratorListApplicationsIntegrationTest extends ApplicationIntegrationTestSupport {

    @Test
    void moderatorCanListApplicationsWithPaginationAndFilters() throws Exception {
        mockMvc.perform(submitApplicationRequest(applicant)).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/applications")
                        .header("Authorization", "Bearer " + accessToken(moderator))
                        .param("page", "1")
                        .param("limit", "20")
                        .param("status", "PENDING")
                        .param("school_year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(20))
                .andExpect(jsonPath("$.total", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }
}
