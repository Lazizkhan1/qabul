package uz.umft.qabul.dto.exam;

import lombok.Data;

import java.util.Map;

@Data
public class SubmitAnswersRequest {
    // Map of questionId -> selected answerId
    private Map<Integer, Integer> answers;
}
