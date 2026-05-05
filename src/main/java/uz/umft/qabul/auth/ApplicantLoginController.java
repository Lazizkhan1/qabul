package uz.umft.qabul.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.auth.dto.ApplicantLoginDtos.ApplicantLoginRequest;
import uz.umft.qabul.auth.dto.TokenResponse;

@RestController
@RequestMapping("/api/v1/auth/applicant")
public class ApplicantLoginController {

    private final ApplicantLoginService service;

    public ApplicantLoginController(ApplicantLoginService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody ApplicantLoginRequest request, HttpServletRequest servletRequest) {
        return service.login(request.phoneNumber(), request.password(), servletRequest.getHeader("User-Agent"));
    }
}
