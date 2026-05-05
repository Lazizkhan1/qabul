package uz.umft.qabul.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.controller.dto.request.LoginRequest;
import uz.umft.qabul.controller.dto.response.TokenResponse;
import uz.umft.qabul.service.ApplicantLoginService;

@RestController
@RequestMapping("/api/v1/auth/applicant")
public class ApplicantLoginController {

    private final ApplicantLoginService service;

    public ApplicantLoginController(ApplicantLoginService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return service.login(request.phoneNumber(), request.password(), servletRequest.getHeader("User-Agent"));
    }
}
