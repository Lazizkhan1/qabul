package uz.umft.qabul.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.Lang;
import uz.umft.qabul.enums.Role;
import uz.umft.qabul.repository.OtpChallengeRepository;
import uz.umft.qabul.repository.SessionRepository;
import uz.umft.qabul.repository.UserRepository;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StaffLoginIntegrationTest {

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

    @Autowired
    AdminBootstrapService adminBootstrapService;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
        otpChallengeRepository.deleteAll();
        userRepository.deleteAll();
        adminBootstrapService.upsertAdmin();

        User moderator = new User();
        moderator.setPhoneNumber("998901234000");
        moderator.setPasswordHash(passwordEncoder.encode("moderator-password"));
        moderator.setType(Role.MODERATOR);
        moderator.setLang(Lang.UZ);
        userRepository.save(moderator);

        User applicant = new User();
        applicant.setPhoneNumber("998902222222");
        applicant.setPasswordHash(passwordEncoder.encode("applicant-password"));
        applicant.setType(Role.APPLICANT);
        applicant.setLang(Lang.UZ);
        userRepository.save(applicant);
    }

    @Test
    void adminAndModeratorCanLoginWithRoleClaims() throws Exception {
        mockMvc.perform(post("/api/v1/auth/staff/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "integration-test")
                        .content("{\"phone_number\":\"998900000001\",\"password\":\"admin-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.refresh_token", notNullValue()))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(post("/api/v1/auth/staff/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "integration-test")
                        .content("{\"phone_number\":\"998901234000\",\"password\":\"moderator-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.refresh_token", notNullValue()))
                .andExpect(jsonPath("$.role").value("MODERATOR"));
    }

    @Test
    void invalidStaffCredentialsAndApplicantStaffLoginAreRejected() throws Exception {
        mockMvc.perform(post("/api/v1/auth/staff/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone_number\":\"998900000001\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

        mockMvc.perform(post("/api/v1/auth/staff/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone_number\":\"998902222222\",\"password\":\"applicant-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }
}
