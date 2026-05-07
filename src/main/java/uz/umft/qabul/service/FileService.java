package uz.umft.qabul.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class FileService {
    
    private final JwtService jwtService;

    public FileService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public Resource loadFileAsResource(String fileId, String token) {
        String verifiedFileId = jwtService.verifyDownloadToken(token);
        if (!verifiedFileId.equals(fileId)) {
            throw uz.umft.qabul.exception.AuthException.unauthorized("INVALID_TOKEN", "Token does not match file ID");
        }
        // TODO: Load file from storage
        return null; 
    }
}
