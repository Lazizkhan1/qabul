package uz.umft.qabul.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionDto {
    private Integer id;
    private String questionText;
    private Double point;
    private Integer subjectId;
}
