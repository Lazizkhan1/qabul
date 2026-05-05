package uz.umft.qabul.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.controller.dto.request.RegisterRequest.ApplicantPhoneRequest;
import uz.umft.qabul.controller.dto.request.RegisterRequest.AuthFlowResponse;
import uz.umft.qabul.controller.dto.request.RegisterRequest.SetPasswordRequest;
import uz.umft.qabul.controller.dto.request.RegisterRequest.VerificationResponse;
import uz.umft.qabul.controller.dto.request.RegisterRequest.VerifyOtpRequest;
import uz.umft.qabul.controller.dto.response.TokenResponse;
import uz.umft.qabul.service.ApplicantRegistrationService;

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
