package uz.umft.qabul;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import uz.umft.qabul.application.ApplicationIntegrationTestSupport;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EndToEndAdmissionWorkflowTest extends ApplicationIntegrationTestSupport {

    @Test
    void testFullAdmissionWorkflow() throws Exception {
        // 1. System Setup
        // Enable Auto-Accept
        mockMvc.perform(patch("/api/v1/applications/settings/auto-accept")
                        .header("Authorization", "Bearer " + accessToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\": true}"))
                .andExpect(status().isOk());

        // Create Exam Questions and Answers (simplified for testing)
        // Question 1
        String q1 = mockMvc.perform(post("/api/v1/exams/management/questions")
                        .header("Authorization", "Bearer " + accessToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "questionText": "Question 1",
                                    "subjectId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer q1Id = extractId(q1);

        // Answers for Q1
        mockMvc.perform(post("/api/v1/exams/management/answers")
                        .header("Authorization", "Bearer " + accessToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "answerText": "Correct Answer",
                                    "questionId": %d,
                                    "correct": true
                                }
                                """.formatted(q1Id)))
                .andExpect(status().isCreated());

        // 2. Application Process
        // Applicant logs in and submits application
        String appResponse = mockMvc.perform(submitApplicationRequest(applicant))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID applicationId = UUID.fromString(extractField(appResponse, "id"));

        // Application should be auto-approved
        mockMvc.perform(get("/api/v1/applications/" + applicationId)
                        .header("Authorization", "Bearer " + accessToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // 3. Examination Process
        // Start Exam Session
        String sessionResponse = mockMvc.perform(post("/api/v1/exams/sessions")
                        .header("Authorization", "Bearer " + accessToken(applicant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "applicationId": "%s"
                                }
                                """.formatted(applicationId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID sessionId = UUID.fromString(extractField(sessionResponse, "id"));

        // Submit Answers
        mockMvc.perform(post("/api/v1/exams/sessions/" + sessionId + "/complete")
                        .header("Authorization", "Bearer " + accessToken(applicant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "answers": {
                                        "%d": 1
                                    }
                                }
                                """.formatted(q1Id)))
                .andExpect(status().isOk());

        // 4. Contract Generation
        mockMvc.perform(get("/api/v1/contracts/download/" + sessionId)
                        .header("Authorization", "Bearer " + accessToken(applicant)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    private Integer extractId(String json) {
        return Integer.parseInt(extractField(json, "id"));
    }

    private String extractField(String json, String fieldName) {
        return json.split("\"" + fieldName + "\":")[1].split(",|}")[0].replace("\"", "").trim();
    }
}
