package uz.umft.qabul.application;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.Lang;
import uz.umft.qabul.enums.Role;
import uz.umft.qabul.repository.OtpChallengeRepository;
import uz.umft.qabul.repository.SessionRepository;
import uz.umft.qabul.repository.UserRepository;
import uz.umft.qabul.service.JwtService;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class ApplicationIntegrationTestSupport {

    protected static final UUID TUITION_ID = UUID.fromString("11111111-1111-7111-8111-111111111111");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected SessionRepository sessionRepository;

    @Autowired
    protected OtpChallengeRepository otpChallengeRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtService jwtTokenService;

    @Autowired
    protected org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    protected User applicant;
    protected User moderator;
    protected User admin;

    @BeforeEach
    void baseSetUp() {
        cleanup();
        seedReferenceData();
        applicant = createUser("998901000001", Role.APPLICANT);
        moderator = createUser("998901000002", Role.MODERATOR);
        admin = createUser("998901000003", Role.ADMIN);
    }

    protected String accessToken(User user) {
        return jwtTokenService.createAccessToken(user);
    }

    protected MockHttpServletRequestBuilder submitApplicationRequest(User user) {
        return post("/api/v1/applications")
                .header("Authorization", "Bearer " + accessToken(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
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
                              "fileUrl":"https://files.example/cert-1.pdf",
                              "categoryId":1
                            }
                          ]
                        }
                        """);
    }

    private User createUser(String phoneNumber, Role role) {
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setPasswordHash(passwordEncoder.encode("StrongPassword123!"));
        user.setType(role);
        user.setLang(Lang.UZ);
        return userRepository.save(user);
    }

    private void cleanup() {
        sessionRepository.deleteAll();
        otpChallengeRepository.deleteAll();
        jdbcTemplate.execute("delete from certs");
        jdbcTemplate.execute("delete from applications");
        jdbcTemplate.execute("delete from application_settings");
        jdbcTemplate.execute("delete from tuition");
        jdbcTemplate.execute("delete from cert_category");
        jdbcTemplate.execute("delete from school_year");
        jdbcTemplate.execute("delete from major");
        jdbcTemplate.execute("delete from major_type");
        jdbcTemplate.execute("delete from major_lang");
        userRepository.deleteAll();
    }

    private void seedReferenceData() {
        jdbcTemplate.update("insert into school_year(id, title, active) values (2026, '2026-2027', 1)");
        jdbcTemplate.update("insert into major(id, title) values (1, 'Computer Science')");
        jdbcTemplate.update("insert into major_type(id, type) values (1, 'Kunduzgi')");
        jdbcTemplate.update("insert into major_lang(id, lang) values (1, 'UZ')");
        jdbcTemplate.update("insert into cert_category(id, title, type) values (1, 'National cert', 1)");
        jdbcTemplate.update("""
                insert into tuition(id, school_year, major_id, major_type_id, major_lang_id, degree, amount, active)
                values (?, 2026, 1, 1, 1, 'BACHELOR', 18000000, 1)
                """, TUITION_ID);
    }
}
