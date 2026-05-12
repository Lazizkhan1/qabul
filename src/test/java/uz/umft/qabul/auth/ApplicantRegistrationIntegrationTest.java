package uz.umft.qabul.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uz.umft.qabul.entity.OtpChallenge;
import uz.umft.qabul.enums.OtpChallengeStatus;
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
class ApplicantRegistrationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    OtpChallengeRepository otpChallengeRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
        otpChallengeRepository.deleteAll();
        jdbcTemplate.execute("truncate table certs cascade ");
        jdbcTemplate.execute("truncate table applications cascade ");
        jdbcTemplate.execute("truncate table application_settings cascade ");
        jdbcTemplate.execute("truncate table certificate_files cascade ");
        userRepository.deleteAll();
    }

    @Test
    void newApplicantCompletesOtpPasswordTokenFlow() throws Exception {
        String phoneNumber = "998901234567";

        mockMvc.perform(post("/api/v1/auth/applicant/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"" + phoneNumber + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flow").value("OTP_VERIFICATION"));
        String firstOtp = latestOtp(phoneNumber);

        mockMvc.perform(post("/api/v1/auth/applicant/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"" + phoneNumber + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flow").value("OTP_VERIFICATION"));
        String latestOtp = latestOtp(phoneNumber);

        mockMvc.perform(post("/api/v1/auth/applicant/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"" + phoneNumber + "\",\"otp\":\"" + firstOtp + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_OTP"));

        String verificationToken = mockMvc.perform(post("/api/v1/auth/applicant/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"" + phoneNumber + "\",\"otp\":\"" + latestOtp + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationToken", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"verificationToken\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/api/v1/auth/applicant/set-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + verificationToken)
                        .header("User-Agent", "integration-test")
                        .content("{\"password\":\"StrongPassword123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.role").value("APPLICANT"))
                .andExpect(jsonPath("$.userId", notNullValue()));

        mockMvc.perform(post("/api/v1/auth/applicant/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"" + phoneNumber + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flow").value("PASSWORD_LOGIN"));
    }

    private String latestOtp(String phoneNumber) {
        OtpChallenge challenge = otpChallengeRepository
                .findFirstByPhoneNumberAndStatusOrderByCreatedAtDesc(phoneNumber, OtpChallengeStatus.ACTIVE)
                .orElseThrow();
        return challenge.getOtpHash();
    }
}
