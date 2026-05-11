package uz.umft.qabul.dto.reference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDto {
    private Integer id;
    private String title;
    private Integer examDuration;
    private Integer totalQuestions;
}
