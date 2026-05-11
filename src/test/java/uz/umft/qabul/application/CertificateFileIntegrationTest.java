package uz.umft.qabul.application;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.Role;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CertificateFileIntegrationTest extends ApplicationIntegrationTestSupport {

    @Test
    void applicantCanUploadAndDownloadOwnCertificateFile() throws Exception {
        String fileUrl = uploadFile(applicant, "cert.pdf", "application/pdf", "pdf-content".getBytes());

        mockMvc.perform(get(fileUrl)
                        .header("Authorization", "Bearer " + accessToken(applicant)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void uploadRejectsUnsupportedFileType() throws Exception {
        mockMvc.perform(multipart("/api/v1/files/certificates")
                        .file(new MockMultipartFile("file", "cert.exe", "application/octet-stream", "x".getBytes()))
                        .param("categoryId", "1")
                        .header("Authorization", "Bearer " + accessToken(applicant)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadRejectsOversizedFile() throws Exception {
        byte[] oversized = new byte[(int) fileProperties.maxSizeBytes() + 1];
        mockMvc.perform(multipart("/api/v1/files/certificates")
                        .file(new MockMultipartFile("file", "cert.pdf", "application/pdf", oversized))
                        .param("categoryId", "1")
                        .header("Authorization", "Bearer " + accessToken(applicant)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonOwnerApplicantCannotDownloadCertificateFile() throws Exception {
        String fileUrl = uploadFile(applicant, "cert.pdf", "application/pdf", "pdf-content".getBytes());
        User anotherApplicant = createUser("998901009999", Role.APPLICANT);

        mockMvc.perform(get(fileUrl)
                        .header("Authorization", "Bearer " + accessToken(anotherApplicant)))
                .andExpect(status().isForbidden());
    }

    @Test
    void moderatorCanDownloadApplicantCertificateFile() throws Exception {
        String fileUrl = uploadFile(applicant, "cert.pdf", "application/pdf", "pdf-content".getBytes());

        mockMvc.perform(get(fileUrl)
                        .header("Authorization", "Bearer " + accessToken(moderator)))
                .andExpect(status().isOk());
    }

    private String uploadFile(User owner, String fileName, String contentType, byte[] content) throws Exception {
        String response = mockMvc.perform(multipart("/api/v1/files/certificates")
                        .file(new MockMultipartFile("file", fileName, contentType, content))
                        .param("categoryId", "1")
                        .header("Authorization", "Bearer " + accessToken(owner)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String marker = "\"fileUrl\":\"";
        int start = response.indexOf(marker);
        if (start < 0) {
            throw new IllegalStateException("fileUrl not found in upload response");
        }
        int valueStart = start + marker.length();
        int end = response.indexOf('"', valueStart);
        return response.substring(valueStart, end);
    }
}
