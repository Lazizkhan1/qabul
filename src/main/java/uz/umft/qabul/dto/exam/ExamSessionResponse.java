package uz.umft.qabul.dto.exam;

import lombok.Builder;
import lombok.Data;
import uz.umft.qabul.enums.ExamSessionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ExamSessionResponse {
    private UUID id;
    private UUID applicationId;
    private ExamSessionStatus status;
    private Double score;
    private LocalDateTime expiresAt;
    private LocalDateTime completedAt;
}
