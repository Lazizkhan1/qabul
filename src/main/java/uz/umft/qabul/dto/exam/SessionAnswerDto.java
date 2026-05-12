package uz.umft.qabul.dto.exam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionAnswerDto {
    private Integer id;
    private String answerText;
}
