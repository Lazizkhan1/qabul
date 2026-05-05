package uz.umft.qabul.auth;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.auth.dto.ApplicantLoginDtos.RefreshTokenRequest;
import uz.umft.qabul.auth.dto.TokenResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class RefreshTokenController {

    private final RefreshTokenService service;

    public RefreshTokenController(RefreshTokenService service) {
        this.service = service;
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return service.refresh(request.refreshToken());
    }
}
