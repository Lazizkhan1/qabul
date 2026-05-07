package uz.umft.qabul.application;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicantDuplicateApplicationIntegrationTest extends ApplicationIntegrationTestSupport {

    @Test
    void duplicateActiveApplicationIsRejected() throws Exception {
        mockMvc.perform(submitApplicationRequest(applicant))
                .andExpect(status().isCreated());

        mockMvc.perform(submitApplicationRequest(applicant))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ACTIVE_APPLICATION_EXISTS"));
    }
}
