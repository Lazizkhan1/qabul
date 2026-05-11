package uz.umft.qabul.application;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.Role;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplicantSubmitApplicationFileReferenceIntegrationTest extends ApplicationIntegrationTestSupport {

    @Test
    void applicantCannotSubmitUsingAnotherApplicantsCertificateFile() throws Exception {
        User anotherApplicant = createUser("998901008888", Role.APPLICANT);
        String foreignFileUrl = createUploadedCertificateFileUrl(anotherApplicant, 1);

        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + accessToken(applicant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationPayload(foreignFileUrl)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CERT_FILE_ACCESS_DENIED"));
    }

    @Test
    void applicantCannotSubmitWithUnknownCertificateFileReference() throws Exception {
        String unknownFileUrl = "/api/v1/files/certificates/00000000-0000-0000-0000-000000000000";

        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + accessToken(applicant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationPayload(unknownFileUrl)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CERT_FILE_REFERENCE"));
    }

    private String applicationPayload(String fileUrl) {
        return """
                {
                  "firstname":"Ali",
                  "lastname":"Valiyev",
                  "middlename":"Rustam o'g'li",
                  "birthDate":"2003-04-15",
                  "gender":"M",
                  "jshshir":"12345678901234",
                  "passportSeries":"AA1234567",
                  "address":"Tashkent, Yunusobod",
                  "additionalPhone":"998901112233",
                  "disability":0,
                  "tuitionId":"11111111-1111-7111-8111-111111111111",
                  "certificates":[
                    {
                      "certNumber":"NAT-2026-0001",
                      "score":78.5,
                      "fileUrl":"%s",
                      "categoryId":1
                    }
                  ]
                }
                """.formatted(fileUrl);
    }
}
