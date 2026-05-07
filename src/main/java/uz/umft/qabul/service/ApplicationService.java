package uz.umft.qabul.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.umft.qabul.controller.dto.application.ApplicationResponse;
import uz.umft.qabul.controller.dto.application.AutoAcceptToggleDto;
import uz.umft.qabul.controller.dto.application.CreateApplicationRequest;
import uz.umft.qabul.dto.PagedResponse;
import uz.umft.qabul.entity.*;
import uz.umft.qabul.enums.ApplicationStatus;
import uz.umft.qabul.enums.Degree;
import uz.umft.qabul.enums.Role;
import uz.umft.qabul.exception.AuthException;
import uz.umft.qabul.repository.*;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ApplicationService {

    private static final String AUTO_ACCEPT_KEY = "AUTO_ACCEPT_APPLICATIONS";
    private static final EnumSet<ApplicationStatus> ACTIVE_STATUSES = EnumSet.of(
            ApplicationStatus.PENDING,
            ApplicationStatus.IN_REVIEW,
            ApplicationStatus.ACCEPTED
    );

    private final ApplicationRepository applicationRepository;
    private final CertRepository certRepository;
    private final TuitionRepository tuitionRepository;
    private final CertCategoryRepository certCategoryRepository;
    private final ApplicationSettingRepository applicationSettingRepository;
    private final ApplicationMapper applicationMapper;
    private final JdbcTemplate jdbcTemplate;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            CertRepository certRepository,
            TuitionRepository tuitionRepository,
            CertCategoryRepository certCategoryRepository,
            ApplicationSettingRepository applicationSettingRepository,
            ApplicationMapper applicationMapper,
            JdbcTemplate jdbcTemplate
    ) {
        this.applicationRepository = applicationRepository;
        this.certRepository = certRepository;
        this.tuitionRepository = tuitionRepository;
        this.certCategoryRepository = certCategoryRepository;
        this.applicationSettingRepository = applicationSettingRepository;
        this.applicationMapper = applicationMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- Submission ---

    @Transactional
    public ApplicationResponse submit(User applicant, CreateApplicationRequest request) {
        if (applicationRepository.existsByUser_IdAndStatusIn(applicant.getId(), ACTIVE_STATUSES)) {
            throw AuthException.conflict("ACTIVE_APPLICATION_EXISTS", "Applicant already has an active application");
        }

        validateCertificateCategories(
                request.certificates().stream().map(CreateApplicationRequest.CertificateInput::categoryId).toList()
        );

        Application application = new Application();
        application.setUser(applicant);
        application.setTuition(tuitionRepository.getReferenceById(request.tuitionId()));
        application.setStatus(isAutoAcceptEnabled() ? ApplicationStatus.ACCEPTED : ApplicationStatus.PENDING);
        application.setFirstname(request.firstname());
        application.setLastname(request.lastname());
        application.setMiddlename(request.middlename());
        application.setBirthDate(request.birthDate());
        application.setGender(request.gender().charAt(0));
        application.setJshshir(request.jshshir());
        application.setPassportSeries(request.passportSeries());
        application.setAddress(request.address());
        application.setAdditionalPhone(request.additionalPhone());
        application.setDisability(request.disability());

        Application savedApplication = applicationRepository.save(application);

        List<Cert> certificates = request.certificates().stream().map(input -> {
            Cert cert = new Cert();
            cert.setApplication(savedApplication);
            cert.setCategory(certCategoryRepository.getReferenceById(input.categoryId()));
            cert.setCertNumber(input.certNumber());
            cert.setScore(input.score());
            cert.setFileUrl(input.fileUrl());
            return cert;
        }).toList();

        List<Cert> savedCertificates = certRepository.saveAll(certificates);
        return applicationMapper.toResponse(savedApplication, savedCertificates);
    }

    // --- Query ---

    @Transactional(readOnly = true)
    public ApplicationResponse getMine(User applicant) {

        var application = applicationRepository.findTopByUser_IdOrderByCreatedAtDesc(applicant.getId())
                .orElseThrow(() -> AuthException.notFound("APPLICATION_NOT_FOUND", "Application not found"));
        return applicationMapper.toResponse(application, certRepository.findByApplication_IdIn(List.of(application.getId())));
    }

    // --- Moderation ---

    @Transactional(readOnly = true)
    public PagedResponse<ApplicationResponse> listForModeration(
            User actor,
            Integer page,
            Integer limit,
            ApplicationStatus status,
            Integer schoolYear,
            Integer majorId,
            Integer majorTypeId,
            Integer majorLangId,
            Degree degree
    ) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedLimit = limit == null || limit < 1 ? 20 : Math.min(limit, 100);

        var result = applicationRepository.findAllForModeration(
                status == null ? null : status.name(),
                schoolYear,
                majorId,
                majorTypeId,
                majorLangId,
                degree == null ? null : degree.name(),
                PageRequest.of(normalizedPage - 1, normalizedLimit)
        );

        List<UUID> applicationIds = result.getContent().stream().map(Application::getId).toList();
        Map<UUID, List<Cert>> certsByApplication = applicationMapper.groupByApplicationId(
                certRepository.findByApplication_IdIn(applicationIds)
        );

        List<ApplicationResponse> data = result.getContent().stream()
                .map(application -> applicationMapper.toResponse(
                        application,
                        certsByApplication.getOrDefault(application.getId(), List.of())
                ))
                .toList();

        return new PagedResponse<>(normalizedPage, normalizedLimit, result.getTotalElements(), data);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getById(User actor, UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> AuthException.notFound("APPLICATION_NOT_FOUND", "Application not found"));
        return applicationMapper.toResponse(application, certRepository.findByApplication_IdIn(List.of(applicationId)));
    }

    @Transactional
    public ApplicationResponse accept(User actor, UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> AuthException.notFound("APPLICATION_NOT_FOUND", "Application not found"));
        if (application.getStatus() != ApplicationStatus.PENDING && application.getStatus() != ApplicationStatus.IN_REVIEW) {
            throw AuthException.badRequest("INVALID_STATUS_TRANSITION", "Only pending or in-review applications can be accepted");
        }
        application.setStatus(ApplicationStatus.ACCEPTED);
        Application saved = applicationRepository.save(application);
        return applicationMapper.toResponse(saved, certRepository.findByApplication_IdIn(List.of(applicationId)));
    }


    @Transactional
    public ApplicationResponse reject(User actor, UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> AuthException.notFound("APPLICATION_NOT_FOUND", "Application not found"));
        if (application.getStatus() == ApplicationStatus.CANCELED) {
            throw AuthException.badRequest("INVALID_STATUS_TRANSITION", "Canceled application cannot be changed");
        }
        if (application.getStatus() == ApplicationStatus.ACCEPTED) {
            throw AuthException.badRequest("INVALID_STATUS_TRANSITION", "Accepted application cannot be rejected");
        }
        application.setStatus(ApplicationStatus.CANCELED);
        Application saved = applicationRepository.save(application);
        return applicationMapper.toResponse(saved, certRepository.findByApplication_IdIn(List.of(applicationId)));
    }

    // --- Settings ---

    @Transactional(readOnly = true)
    public boolean isAutoAcceptEnabled() {
        return applicationSettingRepository.findById(AUTO_ACCEPT_KEY)
                .map(ApplicationSetting::isValueBoolean)
                .orElse(false);
    }

    @Transactional
    public AutoAcceptToggleDto.Response updateAutoAccept(User actor, boolean enabled) {

        ApplicationSetting setting = applicationSettingRepository.findById(AUTO_ACCEPT_KEY)
                .orElseGet(() -> {
                    ApplicationSetting created = new ApplicationSetting();
                    created.setKey(AUTO_ACCEPT_KEY);
                    return created;
                });

        setting.setValueBoolean(enabled);
        setting.setUpdatedAt(LocalDateTime.now());
        setting.setUpdatedBy(actor.getId());
        ApplicationSetting saved = applicationSettingRepository.save(setting);
        return new AutoAcceptToggleDto.Response(saved.isValueBoolean(), saved.getUpdatedAt());
    }

    // --- Helpers / Validation ---

    private void validateCertificateCategories(Collection<Integer> categoryIds) {
        for (Integer categoryId : categoryIds) {
            Integer count = jdbcTemplate.queryForObject(
                    "select count(*) from cert_category where id = ?",
                    Integer.class,
                    categoryId
            );
            if (count == null || count == 0) {
                throw AuthException.badRequest("INVALID_CERT_CATEGORY", "Certificate category does not exist");
            }
        }
    }
}
