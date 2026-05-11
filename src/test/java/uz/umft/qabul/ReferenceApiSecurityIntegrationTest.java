package uz.umft.qabul;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import uz.umft.qabul.application.ApplicationIntegrationTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReferenceApiSecurityIntegrationTest extends ApplicationIntegrationTestSupport {

    @Test
    void listMajors_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/reference/majors"))
                .andExpect(status().isOk());
    }

    @Test
    void listMajorTypes_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/reference/major-types"))
                .andExpect(status().isOk());
    }

    @Test
    void listMajorLangs_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/reference/major-langs"))
                .andExpect(status().isOk());
    }

    @Test
    void listSchoolYears_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/reference/school-years"))
                .andExpect(status().isOk());
    }

    @Test
    void listSubjects_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/reference/subjects"))
                .andExpect(status().isOk());
    }

    @Test
    void listCertCategories_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/reference/cert-categories"))
                .andExpect(status().isOk());
    }

    @Test
    void createMajor_ShouldRequireAdminAuth() throws Exception {
        // Without auth
        mockMvc.perform(post("/api/v1/reference/majors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"New Major\"}"))
                .andExpect(status().isForbidden());

        // With Applicant auth
        mockMvc.perform(post("/api/v1/reference/majors")
                        .header("Authorization", "Bearer " + accessToken(applicant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"New Major\"}"))
                .andExpect(status().isForbidden());

        // With Admin auth
        mockMvc.perform(post("/api/v1/reference/majors")
                        .header("Authorization", "Bearer " + accessToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"New Major\"}"))
                .andExpect(status().isCreated());
    }
}
