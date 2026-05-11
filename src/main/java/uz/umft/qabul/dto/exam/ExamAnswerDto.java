package uz.umft.qabul.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAnswerDto {
    private Integer id;
    private Integer questionId;
    private Boolean isCorrect;
    private String answerText;
}
