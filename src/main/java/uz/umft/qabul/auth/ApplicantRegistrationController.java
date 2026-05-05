package uz.umft.qabul.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.auth.dto.ApplicantRegistrationDtos.ApplicantPhoneRequest;
import uz.umft.qabul.auth.dto.ApplicantRegistrationDtos.AuthFlowResponse;
import uz.umft.qabul.auth.dto.ApplicantRegistrationDtos.SetPasswordRequest;
import uz.umft.qabul.auth.dto.ApplicantRegistrationDtos.VerificationResponse;
import uz.umft.qabul.auth.dto.ApplicantRegistrationDtos.VerifyOtpRequest;
import uz.umft.qabul.auth.dto.TokenResponse;

@RestController
@RequestMapping("/api/v1/auth/applicant")
public class ApplicantRegistrationController {

    private final ApplicantRegistrationService service;

    public ApplicantRegistrationController(ApplicantRegistrationService service) {
        this.service = service;
    }

    @PostMapping("/start")
    public ResponseEntity<AuthFlowResponse> start(@Valid @RequestBody ApplicantPhoneRequest request) {
        AuthFlowResponse response = service.start(request.phoneNumber());
        HttpStatus status = "OTP_VERIFICATION".equals(response.flow()) ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/verify-otp")
    public VerificationResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return service.verifyOtp(request.phoneNumber(), request.otp());
    }

    @PostMapping("/set-password")
    public TokenResponse setPassword(@Valid @RequestBody SetPasswordRequest request, HttpServletRequest servletRequest) {
        return service.setPassword(request.verificationToken(), request.password(), servletRequest.getHeader("User-Agent"));
    }
}
