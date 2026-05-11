package uz.umft.qabul.application;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TuitionIntegrationTest extends ApplicationIntegrationTestSupport {

    @Test
    public void testListTuitionsWithFilters() throws Exception {
        mockMvc.perform(get("/api/v1/tuitions")
                        .param("majorType", "Kunduzgi")
                        .param("majorLang", "UZ")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
