package uz.umft.qabul.controller.dto.application;

import uz.umft.qabul.enums.ApplicationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ApplicationResponse(
        UUID id,
        UUID userId,
        UUID tuitionId,
        ApplicationStatus status,
        String firstname,
        String lastname,
        String middlename,
        LocalDate birthDate,
        String gender,
        String jshshir,
        String passportSeries,
        String address,
        String additionalPhone,
        Integer disability,
        List<CertificateResponse> certificates,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record CertificateResponse(
            UUID id,
            String certNumber,
            String score,
            String fileUrl,
            Integer categoryId
    ) {
    }
}
