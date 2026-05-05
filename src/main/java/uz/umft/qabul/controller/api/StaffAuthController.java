package uz.umft.qabul.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.controller.dto.request.LoginRequest;
import uz.umft.qabul.controller.dto.response.TokenResponse;
import uz.umft.qabul.service.StaffAuthService;

@RestController
@RequestMapping("/api/v1/auth/staff")
public class StaffAuthController {

    private final StaffAuthService service;

    public StaffAuthController(StaffAuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return service.login(request.phoneNumber(), request.password(), servletRequest.getHeader("User-Agent"));
    }
}
