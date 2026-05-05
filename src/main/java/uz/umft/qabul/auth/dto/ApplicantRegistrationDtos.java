package uz.umft.qabul.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ApplicantRegistrationDtos {

    private ApplicantRegistrationDtos() {
    }

    public record ApplicantPhoneRequest(
            @JsonProperty("phone_number")
            @NotBlank
            @Pattern(regexp = "^[0-9]{9,15}$")
            String phoneNumber
    ) {
    }

    public record VerifyOtpRequest(
            @JsonProperty("phone_number")
            @NotBlank
            @Pattern(regexp = "^[0-9]{9,15}$")
            String phoneNumber,

            @NotBlank
            @Pattern(regexp = "^[0-9]{6}$")
            String otp
    ) {
    }

    public record SetPasswordRequest(
            @JsonProperty("verification_token")
            @NotBlank
            String verificationToken,

            @NotBlank
            @Size(min = 8)
            String password
    ) {
    }

    public record AuthFlowResponse(String flow, String message) {
    }

    public record VerificationResponse(
            @JsonProperty("verification_token") String verificationToken,
            @JsonProperty("expires_in") long expiresIn
    ) {
    }
}
