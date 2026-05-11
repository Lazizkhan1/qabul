package uz.umft.qabul.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uz.umft.qabul.config.FileProperties;
import uz.umft.qabul.controller.dto.file.CertificateFileUploadResponse;
import uz.umft.qabul.entity.CertCategory;
import uz.umft.qabul.entity.CertificateFile;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.Role;
import uz.umft.qabul.exception.AuthException;
import uz.umft.qabul.repository.CertCategoryRepository;
import uz.umft.qabul.repository.CertificateFileRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileService {

    private static final String CERTIFICATE_FILE_URL_PREFIX = "/api/v1/files/certificates/";

    private final FileProperties fileProperties;
    private final CertCategoryRepository certCategoryRepository;
    private final CertificateFileRepository certificateFileRepository;
    private final JwtService jwtService;

    public FileService(
            FileProperties fileProperties,
            CertCategoryRepository certCategoryRepository,
            CertificateFileRepository certificateFileRepository,
            JwtService jwtService
    ) {
        this.fileProperties = fileProperties;
        this.certCategoryRepository = certCategoryRepository;
        this.certificateFileRepository = certificateFileRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public CertificateFileUploadResponse uploadCertificate(User applicant, Integer categoryId, MultipartFile file) {
        validateUploadFile(file);
        CertCategory category = certCategoryRepository.findById(categoryId)
                .orElseThrow(() -> AuthException.badRequest("INVALID_CERT_CATEGORY", "Certificate category does not exist"));

        String originalName = normalizeOriginalName(file.getOriginalFilename());
        String extension = extractExtension(originalName);
        String contentType = normalizeContentType(file.getContentType(), extension);

        UUID fileId = UUID.randomUUID();
        Path relativePath = buildRelativePath(fileId, category, applicant.getId(), extension);
        Path absolutePath = resolveStoragePath(relativePath);

        try {
            Files.createDirectories(absolutePath.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, absolutePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store certificate file", ex);
        }

        CertificateFile certificateFile = new CertificateFile();
        certificateFile.setId(fileId);
        certificateFile.setOwnerUser(applicant);
        certificateFile.setCategory(category);
        certificateFile.setRelativePath(relativePath.toString().replace('\\', '/'));
        certificateFile.setOriginalName(originalName);
        certificateFile.setContentType(contentType);
        certificateFile.setSizeBytes(file.getSize());

        try {
            certificateFileRepository.save(certificateFile);
        } catch (RuntimeException ex) {
            try {
                Files.deleteIfExists(absolutePath);
            } catch (IOException ignored) {
            }
            throw ex;
        }

        return new CertificateFileUploadResponse(
                certificateFile.getId(),
                buildCertificateFileUrl(certificateFile.getId(), applicant.getId()),
                certificateFile.getOriginalName(),
                certificateFile.getContentType(),
                certificateFile.getSizeBytes(),
                category.getId()
        );
    }

    @Transactional(readOnly = true)
    public DownloadedFile loadCertificateAsResource(User actor, UUID fileId) {
        CertificateFile certificateFile = certificateFileRepository.findById(fileId)
                .orElseThrow(() -> AuthException.notFound("FILE_NOT_FOUND", "Certificate file not found"));
        ensureCanDownload(actor, certificateFile);

        Path absolutePath = resolveStoragePath(Path.of(certificateFile.getRelativePath()));
        if (!Files.exists(absolutePath)) {
            throw AuthException.notFound("FILE_NOT_FOUND", "Certificate file not found");
        }

        try {
            Resource resource = new UrlResource(absolutePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw AuthException.notFound("FILE_NOT_FOUND", "Certificate file not found");
            }
            return new DownloadedFile(resource, certificateFile.getOriginalName(), certificateFile.getContentType());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load certificate file", ex);
        }
    }

    @Transactional(readOnly = true)
    public void validateOwnedCertificateReference(User applicant, UUID fileId, Integer categoryId) {
        CertificateFile certificateFile = certificateFileRepository.findById(fileId)
                .orElseThrow(() -> AuthException.badRequest("INVALID_CERT_FILE_REFERENCE", "Certificate file reference is invalid"));

        if (!certificateFile.getOwnerUser().getId().equals(applicant.getId())) {
            throw AuthException.forbidden("CERT_FILE_ACCESS_DENIED", "Certificate file does not belong to applicant");
        }
        if (!certificateFile.getCategory().getId().equals(categoryId)) {
            throw AuthException.badRequest("CERT_FILE_CATEGORY_MISMATCH", "Certificate file category does not match");
        }
    }

    public String buildCertificateFileUrl(UUID fileId, UUID userId) {
        String token = jwtService.createDownloadToken(fileId.toString(), userId.toString());
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(CERTIFICATE_FILE_URL_PREFIX)
                .path(fileId.toString())
                .queryParam("token", token)
                .toUriString();
    }

    private void ensureCanDownload(User actor, CertificateFile certificateFile) {
        boolean isStaff = actor.getType() == Role.ADMIN || actor.getType() == Role.MODERATOR;
        if (!isStaff && !certificateFile.getOwnerUser().getId().equals(actor.getId())) {
            throw AuthException.forbidden("FILE_ACCESS_DENIED", "You do not have access to this file");
        }

    }

    private void validateUploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw AuthException.badRequest("EMPTY_FILE", "File is required");
        }
        if (file.getSize() > fileProperties.maxSizeBytes()) {
            throw AuthException.badRequest("FILE_TOO_LARGE", "File exceeds allowed size");
        }
        String originalName = normalizeOriginalName(file.getOriginalFilename());
        String extension = extractExtension(originalName);
        if (extension.isEmpty() || fileProperties.allowedExtensions().stream().noneMatch(ext -> ext.equalsIgnoreCase(extension))) {
            throw AuthException.badRequest("UNSUPPORTED_FILE_TYPE", "File extension is not allowed");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String normalizedContentType = normalizeImageContentType(contentType.trim().toLowerCase(Locale.ROOT));
            boolean allowed = fileProperties.allowedContentTypes().stream()
                    .anyMatch(type -> type.equalsIgnoreCase(normalizedContentType));
            if (!allowed) {
                throw AuthException.badRequest("UNSUPPORTED_FILE_TYPE", "File content type is not allowed");
            }
        }
    }

    private String normalizeOriginalName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "certificate";
        }
        String normalized = Path.of(originalName).getFileName().toString().trim();
        return normalized.isEmpty() ? "certificate" : normalized;
    }

    private String extractExtension(String fileName) {
        int separator = fileName.lastIndexOf('.');
        if (separator < 0 || separator == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(separator + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeContentType(String contentType, String extension) {
        if (contentType == null || contentType.isBlank()) {
            return extensionToMimeType(extension);
        }
        return normalizeImageContentType(contentType.trim().toLowerCase(Locale.ROOT));
    }

    private String normalizeImageContentType(String contentType) {
        return "image/jpg".equals(contentType) ? "image/jpeg" : contentType;
    }

    private String extensionToMimeType(String extension) {
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            default -> "application/octet-stream";
        };
    }

    private Path buildRelativePath(UUID fileId, CertCategory category, UUID ownerUserId, String extension) {
        LocalDate now = LocalDate.now();
        String categorySegment = category.getType().name().toLowerCase(Locale.ROOT) + "-" + category.getType().name();
        String fileName = extension.isBlank() ? fileId.toString() : fileId + "." + extension;
        return Path.of(
                "certificates",
                categorySegment,
                ownerUserId.toString(),
                String.valueOf(now.getYear()),
                String.format("%02d", now.getMonthValue()),
                fileName
        );
    }

    private Path resolveStoragePath(Path relativePath) {
        Path storageRoot = Path.of(fileProperties.baseDir()).toAbsolutePath().normalize();
        Path resolved = storageRoot.resolve(relativePath).normalize();
        if (!resolved.startsWith(storageRoot)) {
            throw AuthException.badRequest("INVALID_FILE_PATH", "File path is invalid");
        }
        return resolved;
    }

    private UUID extractCertificateFileId(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw AuthException.badRequest("INVALID_CERT_FILE_REFERENCE", "Certificate file reference is required");
        }

        String path = fileUrl.trim();
        if (path.startsWith("http://") || path.startsWith("https://")) {
            try {
                path = new URI(path).getPath();
            } catch (URISyntaxException ex) {
                throw AuthException.badRequest("INVALID_CERT_FILE_REFERENCE", "Certificate file reference is invalid");
            }
        }
        if (!path.startsWith(CERTIFICATE_FILE_URL_PREFIX)) {
            throw AuthException.badRequest("INVALID_CERT_FILE_REFERENCE", "Certificate file reference is invalid");
        }
        String rawId = path.substring(CERTIFICATE_FILE_URL_PREFIX.length());
        try {
            return UUID.fromString(rawId);
        } catch (IllegalArgumentException ex) {
            throw AuthException.badRequest("INVALID_CERT_FILE_REFERENCE", "Certificate file reference is invalid");
        }
    }

    public record DownloadedFile(Resource resource, String filename, String contentType) {
    }
}
