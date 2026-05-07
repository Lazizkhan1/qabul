package uz.umft.qabul.application;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AutoAcceptAuthorizationIntegrationTest extends ApplicationIntegrationTestSupport {

    @Test
    void applicantCannotToggleAutoAccept() throws Exception {
        mockMvc.perform(patch("/api/v1/applications/settings/auto-accept")
                        .header("Authorization", "Bearer " + accessToken(applicant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":true}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }
}
