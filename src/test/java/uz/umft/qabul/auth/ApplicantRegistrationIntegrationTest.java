package uz.umft.qabul.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uz.umft.qabul.repository.OtpChallengeRepository;
import uz.umft.qabul.repository.SessionRepository;
import uz.umft.qabul.repository.UserRepository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class ApplicantRegistrationIntegrationTest {

    private static final Pattern OTP_PATTERN = Pattern.compile("Generated local-dev OTP for applicant phone (\\d+): (\\d{6})");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    OtpChallengeRepository otpChallengeRepository;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
        otpChallengeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void newApplicantCompletesOtpPasswordTokenFlow(CapturedOutput output) throws Exception {
        String phoneNumber = "998901234567";

        mockMvc.perform(post("/api/v1/auth/applicant/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone_number\":\"" + phoneNumber + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flow").value("OTP_VERIFICATION"));
        String firstOtp = latestOtp(output, phoneNumber);

        mockMvc.perform(post("/api/v1/auth/applicant/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone_number\":\"" + phoneNumber + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flow").value("OTP_VERIFICATION"));
        String latestOtp = latestOtp(output, phoneNumber);

        mockMvc.perform(post("/api/v1/auth/applicant/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone_number\":\"" + phoneNumber + "\",\"otp\":\"" + firstOtp + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_OTP"));

        String verificationToken = mockMvc.perform(post("/api/v1/auth/applicant/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone_number\":\"" + phoneNumber + "\",\"otp\":\"" + latestOtp + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verification_token", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"verification_token\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/api/v1/auth/applicant/set-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "integration-test")
                        .content("{\"verification_token\":\"" + verificationToken + "\",\"password\":\"StrongPassword123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.refresh_token", notNullValue()))
                .andExpect(jsonPath("$.role").value("APPLICANT"))
                .andExpect(jsonPath("$.user_id", notNullValue()));

        mockMvc.perform(post("/api/v1/auth/applicant/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone_number\":\"" + phoneNumber + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flow").value("PASSWORD_LOGIN"));
    }

    private String latestOtp(CapturedOutput output, String phoneNumber) {
        Matcher matcher = OTP_PATTERN.matcher(output.getOut());
        String otp = null;
        while (matcher.find()) {
            if (phoneNumber.equals(matcher.group(1))) {
                otp = matcher.group(2);
            }
        }
        if (otp == null) {
            throw new AssertionError("OTP was not printed to the application console");
        }
        return otp;
    }
}
