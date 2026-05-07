package uz.umft.qabul.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uz.umft.qabul.entity.Session;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.Lang;
import uz.umft.qabul.enums.Role;
import uz.umft.qabul.enums.SessionStatus;
import uz.umft.qabul.repository.OtpChallengeRepository;
import uz.umft.qabul.repository.SessionRepository;
import uz.umft.qabul.repository.UserRepository;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicantLoginIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    OtpChallengeRepository otpChallengeRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
        otpChallengeRepository.deleteAll();
        userRepository.deleteAll();

        User applicant = new User();
        applicant.setPhoneNumber("998901111111");
        applicant.setPasswordHash(passwordEncoder.encode("StrongPassword123!"));
        applicant.setType(Role.APPLICANT);
        applicant.setLang(Lang.UZ);
        userRepository.save(applicant);
    }

    @Test
    void applicantCanLoginAndRefreshToken() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/v1/auth/applicant/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "integration-test")
                        .content("{\"phoneNumber\":\"998901111111\",\"password\":\"StrongPassword123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.role").value("APPLICANT"))
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = loginResponse.replaceAll(".*\"refreshToken\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.role").value("APPLICANT"));

        Session session = sessionRepository.findByTokenAndStatus(refreshToken, SessionStatus.ACTIVE).orElseThrow();
        session.setExpiredAt(LocalDateTime.now().minusMinutes(1));
        sessionRepository.save(session);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("EXPIRED_REFRESH_TOKEN"));
    }

    @Test
    void wrongApplicantPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/auth/applicant/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"998901111111\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }
}
