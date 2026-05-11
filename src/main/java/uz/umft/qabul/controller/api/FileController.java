package uz.umft.qabul.controller.api;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.umft.qabul.controller.dto.file.CertificateFileUploadResponse;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.service.FileService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/certificates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('APPLICANT')")
    public CertificateFileUploadResponse uploadCertificate(
            @AuthenticationPrincipal User user,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam("file") MultipartFile file
    ) {
        return fileService.uploadCertificate(user, categoryId, file);
    }

    @GetMapping("/certificates/{fileId}")
    @PreAuthorize("hasAnyRole('APPLICANT','ADMIN','MODERATOR')")
    public ResponseEntity<Resource> downloadCertificate(
            @AuthenticationPrincipal User user,
            @PathVariable UUID fileId
    ) {
        FileService.DownloadedFile downloadedFile = fileService.loadCertificateAsResource(user, fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadedFile.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadedFile.filename() + "\"")
                .body(downloadedFile.resource());
    }
}
