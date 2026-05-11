package uz.umft.qabul.controller.dto.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateApplicationRequest(
        @NotBlank String firstname,
        @NotBlank String lastname,
        String middlename,
        @NotNull LocalDate birthDate,
        @NotBlank @Pattern(regexp = "^[MF]$") String gender,
        @NotBlank String jshshir,
        @NotBlank String passportSeries,
        @NotBlank String address,
        String additionalPhone,
        @Min(0) Integer disability,
        @NotNull UUID tuitionId,
        List<@Valid CertificateInput> certificates
) {
    public record CertificateInput(
            @NotBlank String certNumber,
            @NotBlank String score,
            @NotNull UUID fileId,
            @NotNull Integer categoryId
    ) {
    }
}
