package uz.umft.qabul.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uz.umft.qabul.entity.OtpChallenge;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.Lang;
import uz.umft.qabul.enums.Role;
import uz.umft.qabul.repository.OtpChallengeRepository;
import uz.umft.qabul.repository.SessionRepository;
import uz.umft.qabul.repository.UserRepository;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicantPasswordResetIntegrationTest {

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
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
        otpChallengeRepository.deleteAll();
        jdbcTemplate.execute("delete from certs");
        jdbcTemplate.execute("delete from applications");
        jdbcTemplate.execute("delete from application_settings");
        jdbcTemplate.execute("delete from certificate_files");
        userRepository.deleteAll();
    }

    @Test
    void applicantCanResetPasswordWithOtp() throws Exception {
        String phoneNumber = "998901111111";
        String oldPassword = "OldPassword123!";
        String newPassword = "NewPassword123!";

        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setPasswordHash(passwordEncoder.encode(oldPassword));
        user.setType(Role.APPLICANT);
        user.setLang(Lang.UZ);
        userRepository.save(user);

        mockMvc.perform(post("/api/v1/auth/applicant/reset/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"" + phoneNumber + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flow").value("OTP_VERIFICATION"));

        String otp = latestOtp(phoneNumber);

        String resetToken = mockMvc.perform(post("/api/v1/auth/applicant/reset/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"" + phoneNumber + "\",\"otp\":\"" + otp + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationToken", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"verificationToken\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/api/v1/auth/applicant/reset/set-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + resetToken)
                        .header("User-Agent", "integration-test")
                        .content("{\"password\":\"" + newPassword + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.role").value("APPLICANT"))
                .andExpect(jsonPath("$.userId", notNullValue()));

        mockMvc.perform(post("/api/v1/auth/applicant/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"" + phoneNumber + "\",\"password\":\"" + oldPassword + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

        mockMvc.perform(post("/api/v1/auth/applicant/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"" + phoneNumber + "\",\"password\":\"" + newPassword + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()));
    }

    private String latestOtp(String phoneNumber) {
        OtpChallenge challenge = otpChallengeRepository
                .findFirstByPhoneNumberAndStatusOrderByCreatedAtDesc(phoneNumber, uz.umft.qabul.enums.OtpChallengeStatus.ACTIVE)
                .orElseThrow();
        return challenge.getOtpHash();
    }
}
