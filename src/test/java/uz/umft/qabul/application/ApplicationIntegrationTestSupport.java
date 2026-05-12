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
import uz.umft.qabul.config.FileProperties;
import uz.umft.qabul.entity.CertificateFile;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.Lang;
import uz.umft.qabul.enums.Role;
import uz.umft.qabul.repository.*;
import uz.umft.qabul.service.JwtService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
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

    @Autowired
    protected CertificateFileRepository certificateFileRepository;

    @Autowired
    protected CertCategoryRepository certCategoryRepository;

    @Autowired
    protected FileProperties fileProperties;

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
        String certificateFileId = createUploadedCertificateFileId(user, 1);
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
                              "fileId":"%s",
                              "categoryId":1
                            }
                          ]
                        }
                        """.formatted(certificateFileId));
    }

    protected User createUser(String phoneNumber, Role role) {
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setPasswordHash(passwordEncoder.encode("StrongPassword123!"));
        user.setType(role);
        user.setLang(Lang.UZ);
        return userRepository.save(user);
    }

    private void cleanup() {
        clearStorageDirectory();
        sessionRepository.deleteAll();
        otpChallengeRepository.deleteAll();
        jdbcTemplate.execute("truncate table tuition cascade");
        jdbcTemplate.execute("truncate table school_year cascade");
        jdbcTemplate.execute("truncate table major cascade");
        jdbcTemplate.execute("truncate table major_type cascade");
        jdbcTemplate.execute("truncate table major_lang cascade");
        jdbcTemplate.execute("truncate table exam_questions cascade");
        jdbcTemplate.execute("truncate table contracts cascade");
        jdbcTemplate.execute("truncate table exam_session_answers cascade");
        jdbcTemplate.execute("truncate table exam_sessions cascade");
        jdbcTemplate.execute("truncate table exam_subjects cascade");
        jdbcTemplate.execute("truncate table exam_answers cascade");
        jdbcTemplate.execute("truncate table exams cascade");
        jdbcTemplate.execute("truncate table certificate_files cascade");
        jdbcTemplate.execute("truncate table certs cascade");
        jdbcTemplate.execute("truncate table bachelor_certs cascade");
        jdbcTemplate.execute("truncate table applications cascade");
        jdbcTemplate.execute("truncate table application_settings cascade");
        jdbcTemplate.execute("truncate table subjects cascade");
        jdbcTemplate.execute("truncate table cert_category cascade");
        userRepository.deleteAll();
    }

    protected String createUploadedCertificateFileId(User owner, Integer categoryId) {
        UUID fileId = UUID.randomUUID();
        String relativePath = "certificates/test-" + categoryId + "/" + owner.getId() + "/" + fileId + ".pdf";
        Path absolutePath = Path.of(fileProperties.baseDir()).toAbsolutePath().normalize().resolve(relativePath).normalize();

        try {
            Files.createDirectories(absolutePath.getParent());
            Files.writeString(absolutePath, "test pdf content");
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write test certificate file", ex);
        }

        CertificateFile certificateFile = new CertificateFile();
        certificateFile.setId(fileId);
        certificateFile.setOwnerUser(owner);
        certificateFile.setCategory(certCategoryRepository.getReferenceById(categoryId));
        certificateFile.setRelativePath(relativePath);
        certificateFile.setOriginalName("test-certificate.pdf");
        certificateFile.setContentType("application/pdf");
        certificateFile.setSizeBytes(absolutePath.toFile().length());
        certificateFileRepository.save(certificateFile);

        return fileId.toString();
    }

    private void clearStorageDirectory() {
        Path storageDir = Path.of(fileProperties.baseDir()).toAbsolutePath().normalize();
        if (!Files.exists(storageDir)) {
            return;
        }
        try (var paths = Files.walk(storageDir)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ex) {
                            throw new IllegalStateException("Failed to delete test storage file", ex);
                        }
                    });
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to clean test storage directory", ex);
        }
    }

    private void seedReferenceData() {
        jdbcTemplate.update("insert into school_year(id, title, active) values (2026, '2026-2027', true)");
        jdbcTemplate.update("insert into major(id, title) values (1, 'Computer Science')");
        jdbcTemplate.update("insert into major_type(id, type) values (1, 'Kunduzgi')");
        jdbcTemplate.update("insert into major_lang(id, lang) values (1, 'UZ')");
        jdbcTemplate.update("insert into cert_category(id, title, type) values (1, 'National cert', 'NATIONAL')");
        jdbcTemplate.update("insert into subjects(id, title) values (1, 'Mathematics')");
        jdbcTemplate.update("""
                insert into tuition(id, school_year, major_id, major_type_id, major_lang_id, degree, amount)
                values (?, 2026, 1, 1, 1, 'BACHELOR', 18000000)
                """, TUITION_ID);
    }
}
