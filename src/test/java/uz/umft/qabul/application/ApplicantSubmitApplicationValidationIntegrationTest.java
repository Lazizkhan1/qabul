package uz.umft.qabul.application;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicantSubmitApplicationValidationIntegrationTest extends ApplicationIntegrationTestSupport {

    @Test
    void missingRequiredFieldsReturnValidationErrors() throws Exception {
        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + accessToken(applicant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lastname":"Valiyev",
                                  "birthDate":"2003-04-15",
                                  "gender":"M",
                                  "jshshir":"12345678901234",
                                  "passportSeries":"AA1234567",
                                  "address":"Tashkent",
                                  "tuitionId":"11111111-1111-7111-8111-111111111111",
                                  "certificates":[{"certNumber":"C1","score":80,"fileUrl":"https://x","categoryId":1}]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.firstname").exists());
    }
}
