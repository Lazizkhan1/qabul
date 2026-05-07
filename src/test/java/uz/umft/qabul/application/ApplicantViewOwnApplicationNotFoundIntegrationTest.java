package uz.umft.qabul.application;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicantViewOwnApplicationNotFoundIntegrationTest extends ApplicationIntegrationTestSupport {

    @Test
    void applicantWithoutApplicationGetsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/applications/me")
                        .header("Authorization", "Bearer " + accessToken(applicant)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("APPLICATION_NOT_FOUND"));
    }
}
