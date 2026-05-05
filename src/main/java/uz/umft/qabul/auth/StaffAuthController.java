package uz.umft.qabul.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.auth.dto.StaffLoginDtos.StaffLoginRequest;
import uz.umft.qabul.auth.dto.TokenResponse;

@RestController
@RequestMapping("/api/v1/auth/staff")
public class StaffAuthController {

    private final StaffAuthService service;

    public StaffAuthController(StaffAuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody StaffLoginRequest request, HttpServletRequest servletRequest) {
        return service.login(request.username(), request.password(), servletRequest.getHeader("User-Agent"));
    }
}
