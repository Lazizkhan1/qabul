package uz.umft.qabul.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public final class ApplicantLoginDtos {

    private ApplicantLoginDtos() {
    }

    public record ApplicantLoginRequest(
            @JsonProperty("phone_number")
            @NotBlank
            @Pattern(regexp = "^[0-9]{9,15}$")
            String phoneNumber,

            @NotBlank
            String password
    ) {
    }

    public record RefreshTokenRequest(
            @JsonProperty("refresh_token")
            @NotBlank
            String refreshToken
    ) {
    }
}
