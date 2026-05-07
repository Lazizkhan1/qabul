package uz.umft.qabul.application;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ModeratorAcceptApplicationIntegrationTest extends ApplicationIntegrationTestSupport {

    @Test
    void moderatorCanAcceptPendingApplication() throws Exception {
        String payload = mockMvc.perform(submitApplicationRequest(applicant))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String applicationId = payload.replaceAll("^\\{\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/api/v1/applications/{applicationId}/accept", applicationId)
                        .header("Authorization", "Bearer " + accessToken(moderator)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }
}
