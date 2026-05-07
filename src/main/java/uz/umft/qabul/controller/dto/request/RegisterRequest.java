package uz.umft.qabul.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class RegisterRequest {

    private RegisterRequest() {
    }

    public record ApplicantPhoneRequest(
            @NotBlank
            @Pattern(regexp = "^[0-9]{9,15}$")
            String phoneNumber
    ) {
    }

    public record VerifyOtpRequest(
            @NotBlank
            @Pattern(regexp = "^[0-9]{9,15}$")
            String phoneNumber,

            @NotBlank
            @Pattern(regexp = "^[0-9]{6}$")
            String otp
    ) {
    }

    public record SetPasswordRequest(
            @NotBlank
            @Size(min = 8)
            String password
    ) {
    }

    public record AuthFlowResponse(String flow, String message) {
    }

    public record VerificationResponse(
            String verificationToken,
            long expiresIn
    ) {
    }
}
