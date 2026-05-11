package uz.umft.qabul.controller.dto.file;

import java.util.UUID;

public record CertificateFileUploadResponse(
        UUID fileId,
        String fileUrl,
        String originalName,
        String contentType,
        long sizeBytes,
        Integer categoryId
) {
}
