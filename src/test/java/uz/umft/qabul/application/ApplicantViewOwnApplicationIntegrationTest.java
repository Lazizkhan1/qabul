package uz.umft.qabul.application;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicantViewOwnApplicationIntegrationTest extends ApplicationIntegrationTestSupport {

    @Test
    void applicantCanViewOwnApplication() throws Exception {
        mockMvc.perform(submitApplicationRequest(applicant)).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/applications/me")
                        .header("Authorization", "Bearer " + accessToken(applicant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.status").exists());
    }
}
