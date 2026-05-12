package uz.umft.qabul.dto.exam;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SessionQuestionDto {
    private Integer id;
    private String questionText;
    private Double point;
    private Integer subjectId;
    private List<SessionAnswerDto> answers;
}
