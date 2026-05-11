package uz.umft.qabul.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.controller.dto.application.ApplicationResponse;
import uz.umft.qabul.controller.dto.application.AutoAcceptToggleDto;
import uz.umft.qabul.controller.dto.application.CreateApplicationRequest;
import uz.umft.qabul.dto.PagedResponse;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.ApplicationStatus;
import uz.umft.qabul.enums.Degree;
import uz.umft.qabul.service.ApplicationService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('APPLICANT')")
    public ResponseEntity<ApplicationResponse> submit(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateApplicationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.submit(user, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public PagedResponse<ApplicationResponse> list(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(name = "school_year", required = false) Integer schoolYear,
            @RequestParam(name = "major_id", required = false) Integer majorId,
            @RequestParam(name = "major_type_id", required = false) Integer majorTypeId,
            @RequestParam(name = "major_lang_id", required = false) Integer majorLangId,
            @RequestParam(required = false) Degree degree
    ) {
        return applicationService.listForModeration(
                user, page, limit, status, schoolYear, majorId, majorTypeId, majorLangId, degree
        );
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('APPLICANT')")
    public ApplicationResponse getMine(@AuthenticationPrincipal User user) {
        return applicationService.getMine(user);
    }

    @GetMapping("/{applicationId}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ApplicationResponse getById(
            @AuthenticationPrincipal User user,
            @PathVariable UUID applicationId
    ) {
        return applicationService.getById(user, applicationId);
    }

    @PostMapping("/{applicationId}/accept")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ApplicationResponse accept(
            @AuthenticationPrincipal User user,
            @PathVariable UUID applicationId
    ) {
        return applicationService.accept(user, applicationId);
    }

    @PostMapping("/{applicationId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ApplicationResponse reject(
            @AuthenticationPrincipal User user,
            @PathVariable UUID applicationId
    ) {
        return applicationService.reject(user, applicationId);
    }

    @PutMapping("/{applicationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApplicationResponse update(@PathVariable UUID applicationId, @RequestBody CreateApplicationRequest request) {
        return applicationService.update(applicationId, request);
    }

    @DeleteMapping("/{applicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID applicationId) {
        applicationService.delete(applicationId);
    }

    @PatchMapping("/settings/auto-accept")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public AutoAcceptToggleDto.Response updateAutoAccept(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AutoAcceptToggleDto.Request request
    ) {
        return applicationService.updateAutoAccept(user, request.enabled());
    }
}
