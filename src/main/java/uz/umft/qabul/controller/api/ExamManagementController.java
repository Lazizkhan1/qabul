package uz.umft.qabul.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.dto.exam.ExamAnswerDto;
import uz.umft.qabul.dto.exam.ExamQuestionDto;
import uz.umft.qabul.service.ExamManagementService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams/management")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ExamManagementController {

    private final ExamManagementService examManagementService;

    // Questions
    @GetMapping("/questions")
    public List<ExamQuestionDto> listQuestions() {
        return examManagementService.listQuestions();
    }

    @PostMapping("/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public ExamQuestionDto createQuestion(@RequestBody ExamQuestionDto question) {
        return examManagementService.createQuestion(question);
    }

    @PutMapping("/questions/{id}")
    public ExamQuestionDto updateQuestion(@PathVariable Integer id, @RequestBody ExamQuestionDto question) {
        return examManagementService.updateQuestion(id, question);
    }

    @DeleteMapping("/questions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuestion(@PathVariable Integer id) {
        examManagementService.deleteQuestion(id);
    }

    // Answers
    @GetMapping("/answers")
    public List<ExamAnswerDto> listAnswers() {
        return examManagementService.listAnswers();
    }

    @PostMapping("/answers")
    @ResponseStatus(HttpStatus.CREATED)
    public ExamAnswerDto createAnswer(@RequestBody ExamAnswerDto answer) {
        return examManagementService.createAnswer(answer);
    }

    @PutMapping("/answers/{id}")
    public ExamAnswerDto updateAnswer(@PathVariable Integer id, @RequestBody ExamAnswerDto answer) {
        return examManagementService.updateAnswer(id, answer);
    }

    @DeleteMapping("/answers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAnswer(@PathVariable Integer id) {
        examManagementService.deleteAnswer(id);
    }

    @PostMapping("/questions/import")
    public uz.umft.qabul.dto.exam.ImportSummaryDto importQuestions(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("subjectId") Integer subjectId) {
        return examManagementService.importQuestionsFromDocx(file, subjectId);
    }
}
