package uz.umft.qabul;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import uz.umft.qabul.application.ApplicationIntegrationTestSupport;
import uz.umft.qabul.entity.Application;
import uz.umft.qabul.entity.ExamSession;
import uz.umft.qabul.entity.Tuition;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.ApplicationStatus;
import uz.umft.qabul.enums.ExamSessionStatus;
import uz.umft.qabul.enums.Role;
import uz.umft.qabul.repository.ApplicationRepository;
import uz.umft.qabul.repository.ExamSessionRepository;
import uz.umft.qabul.repository.TuitionRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ContractDownloadIntegrationTest extends ApplicationIntegrationTestSupport {

    @Autowired
    private ExamSessionRepository examSessionRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private TuitionRepository tuitionRepository;

    @Test
    void applicantCanDownloadContractAfterSuccessfulExam() throws Exception {
        // Given: An application and a completed exam session with a score of 60.0
        Tuition tuition = tuitionRepository.findById(TUITION_ID).orElseThrow();

        Application app = new Application();
        app.setUser(applicant);
        app.setTuition(tuition);
        app.setStatus(ApplicationStatus.ACCEPTED);
        app.setFirstname("Ali");
        app.setLastname("Valiyev");
        app.setBirthDate(java.time.LocalDate.of(2000, 1, 1));
        app.setGender('M');
        app.setJshshir("12345678901234");
        app.setPassportSeries("AA1234567");
        app.setAddress("Tashkent");
        app.setDisability(0);
        app = applicationRepository.save(app);

        ExamSession session = new ExamSession();
        session.setApplication(app);
        session.setStatus(ExamSessionStatus.COMPLETED);
        session.setScore(60.0);
        session.setExpiresAt(LocalDateTime.now().plusHours(1));
        session.setCompletedAt(LocalDateTime.now());
        session = examSessionRepository.save(session);

        // When: Applicant tries to download the contract
        MvcResult result = mockMvc.perform(get("/api/v1/contracts/download/{sessionId}", session.getId())
                        .header("Authorization", "Bearer " + accessToken(applicant)))
                // Then: Status is 200, content type is PDF
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"attachment\"; filename=\"contract.pdf\""))
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        assertTrue(content.length > 0, "PDF content should not be empty");
        // Simple check for PDF header %PDF-
        assertEquals('%', (char) content[0]);
        assertEquals('P', (char) content[1]);
        assertEquals('D', (char) content[2]);
        assertEquals('F', (char) content[3]);
    }

    @Test
    void applicantCannotDownloadContractOfAnotherUser() throws Exception {
        // Given: Another applicant with a completed exam
        User otherApplicant = createUser("998909999999", Role.APPLICANT);
        Tuition tuition = tuitionRepository.findById(TUITION_ID).orElseThrow();

        Application app = new Application();
        app.setUser(otherApplicant);
        app.setTuition(tuition);
        app.setStatus(ApplicationStatus.ACCEPTED);
        app.setFirstname("Bob");
        app.setLastname("Smith");
        app.setBirthDate(java.time.LocalDate.of(2000, 1, 1));
        app.setGender('M');
        app.setJshshir("98765432109876");
        app.setPassportSeries("BB1234567");
        app.setAddress("Tashkent");
        app.setDisability(0);
        app = applicationRepository.save(app);

        ExamSession session = new ExamSession();
        session.setApplication(app);
        session.setStatus(ExamSessionStatus.COMPLETED);
        session.setScore(60.0);
        session.setExpiresAt(LocalDateTime.now().plusHours(1));
        session.setCompletedAt(LocalDateTime.now());
        session = examSessionRepository.save(session);

        // When: The first applicant tries to download the second's contract
        mockMvc.perform(get("/api/v1/contracts/download/{sessionId}", session.getId())
                        .header("Authorization", "Bearer " + accessToken(applicant)))
                // Then: Status is 403 Forbidden
                .andExpect(status().isForbidden());
    }
}
