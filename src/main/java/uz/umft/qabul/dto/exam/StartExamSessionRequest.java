package uz.umft.qabul.dto.exam;

import lombok.Data;

import java.util.UUID;

@Data
public class StartExamSessionRequest {
    private UUID applicationId;
}
