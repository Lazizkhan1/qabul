package uz.umft.qabul.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.dto.exam.ExamSessionResponse;
import uz.umft.qabul.dto.exam.SessionQuestionDto;
import uz.umft.qabul.dto.exam.StartExamSessionRequest;
import uz.umft.qabul.dto.exam.SubmitAnswersRequest;
import uz.umft.qabul.service.ExamService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exams/sessions")
@PreAuthorize("hasRole('APPLICANT')")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExamSessionResponse startSession(@RequestBody StartExamSessionRequest request) {
        return examService.startSession(request);
    }

    @GetMapping("/{sessionId}/questions")
    public List<SessionQuestionDto> getQuestions(@PathVariable UUID sessionId) {
        return examService.getSessionQuestions(sessionId);
    }

    @PostMapping("/{sessionId}/complete")
    public ExamSessionResponse completeSession(@PathVariable UUID sessionId, @RequestBody SubmitAnswersRequest request) {
        return examService.completeSession(sessionId, request);
    }
}
