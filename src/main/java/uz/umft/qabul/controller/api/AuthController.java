package uz.umft.qabul.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.controller.dto.request.LoginRequest;
import uz.umft.qabul.controller.dto.request.RegisterRequest.ApplicantPhoneRequest;
import uz.umft.qabul.controller.dto.request.RegisterRequest.AuthFlowResponse;
import uz.umft.qabul.controller.dto.request.RegisterRequest.SetPasswordRequest;
import uz.umft.qabul.controller.dto.request.RegisterRequest.VerificationResponse;
import uz.umft.qabul.controller.dto.request.RegisterRequest.VerifyOtpRequest;
import uz.umft.qabul.controller.dto.response.TokenResponse;
import uz.umft.qabul.exception.AuthException;
import uz.umft.qabul.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/applicant/login")
    @PreAuthorize("permitAll()")
    public TokenResponse loginApplicant(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return service.loginApplicant(request.phoneNumber(), request.password(), servletRequest.getHeader("User-Agent"));
    }

    @PostMapping("/applicant/start")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthFlowResponse> startRegistration(@Valid @RequestBody ApplicantPhoneRequest request) {
        AuthFlowResponse response = service.startRegistration(request.phoneNumber());
        HttpStatus status = "OTP_VERIFICATION".equals(response.flow()) ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/applicant/verify-otp")
    @PreAuthorize("permitAll()")
    public VerificationResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return service.verifyOtp(request.phoneNumber(), request.otp());
    }

    @PostMapping("/applicant/set-password")
    @PreAuthorize("permitAll()")
    public TokenResponse setPassword(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody SetPasswordRequest request,
            HttpServletRequest servletRequest
    ) {
        String verificationToken = extractBearerToken(authorization, "INVALID_VERIFICATION_TOKEN", "Verification token is invalid");
        return service.completeRegistration(verificationToken, request.password(), servletRequest.getHeader("User-Agent"));
    }

    @PostMapping("/applicant/reset/start")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthFlowResponse> startPasswordReset(@Valid @RequestBody ApplicantPhoneRequest request) {
        AuthFlowResponse response = service.startPasswordReset(request.phoneNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/applicant/reset/verify-otp")
    @PreAuthorize("permitAll()")
    public VerificationResponse verifyResetOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return service.verifyResetOtp(request.phoneNumber(), request.otp());
    }

    @PostMapping("/applicant/reset/set-password")
    @PreAuthorize("permitAll()")
    public TokenResponse resetPassword(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody SetPasswordRequest request,
            HttpServletRequest servletRequest
    ) {
        String resetToken = extractBearerToken(authorization, "INVALID_RESET_TOKEN", "Reset token is invalid");
        return service.resetPassword(resetToken, request.password(), servletRequest.getHeader("User-Agent"));
    }

    private String extractBearerToken(String authorization, String code, String message) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw AuthException.unauthorized(code, message);
        }
        return authorization.substring("Bearer ".length());
    }

    @PostMapping("/staff/login")
    @PreAuthorize("permitAll()")
    public TokenResponse loginStaff(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return service.loginStaff(request.phoneNumber(), request.password(), servletRequest.getHeader("User-Agent"));
    }

    @PostMapping("/refresh")
    @PreAuthorize("permitAll()")
    public TokenResponse refresh(@CookieValue(value = "refresh_token", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw AuthException.unauthorized("INVALID_REFRESH_TOKEN", "Refresh token is invalid");
        }
        return service.refresh(refreshToken);
    }
}
