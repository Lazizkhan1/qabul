package uz.umft.qabul.dto.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDto {
    private UUID id;
    private String contractUrl;
    private UUID examId;
    private Double scale;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
