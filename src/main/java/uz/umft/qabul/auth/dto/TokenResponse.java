package uz.umft.qabul.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import uz.umft.qabul.domain.enums.Role;

import java.util.UUID;

public record TokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn,
        @JsonProperty("refresh_expires_in") long refreshExpiresIn,
        @JsonProperty("user_id") UUID userId,
        Role role
) {
    public TokenResponse(String accessToken, String refreshToken, long expiresIn, long refreshExpiresIn, UUID userId, Role role) {
        this(accessToken, refreshToken, "Bearer", expiresIn, refreshExpiresIn, userId, role);
    }
}
