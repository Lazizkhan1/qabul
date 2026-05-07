package uz.umft.qabul.controller.dto.response;

import uz.umft.qabul.enums.Role;

import java.util.UUID;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        long refreshExpiresIn,
        UUID userId,
        Role role
) {
    public TokenResponse(String accessToken, String refreshToken, long expiresIn, long refreshExpiresIn, UUID userId, Role role) {
        this(accessToken, refreshToken, "Bearer", expiresIn, refreshExpiresIn, userId, role);
    }
}
