package uz.umft.qabul.application;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AutoAcceptToggleIntegrationTest extends ApplicationIntegrationTestSupport {

    @Test
    void moderatorCanToggleAutoAccept() throws Exception {
        mockMvc.perform(patch("/api/v1/applications/settings/auto-accept")
                        .header("Authorization", "Bearer " + accessToken(moderator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.updatedAt").exists());
    }
}
