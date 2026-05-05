package uz.umft.qabul.controller.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.controller.dto.request.RefreshTokenRequest;
import uz.umft.qabul.controller.dto.response.TokenResponse;
import uz.umft.qabul.service.RefreshTokenService;

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
