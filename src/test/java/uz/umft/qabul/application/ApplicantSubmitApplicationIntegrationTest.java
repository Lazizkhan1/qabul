package uz.umft.qabul.application;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicantSubmitApplicationIntegrationTest extends ApplicationIntegrationTestSupport {

    @Test
    void applicantCanSubmitApplication() throws Exception {
        mockMvc.perform(submitApplicationRequest(applicant))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.certificates[0].certNumber").value("NAT-2026-0001"));

        mockMvc.perform(patch("/api/v1/applications/settings/auto-accept")
                        .header("Authorization", "Bearer " + accessToken(moderator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }
}
