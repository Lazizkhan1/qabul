package uz.umft.qabul.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.dto.exam.ExamDto;
import uz.umft.qabul.service.ExamService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exams")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public List<ExamDto> list() {
        return examService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExamDto create(@RequestBody ExamDto exam) {
        return examService.create(exam);
    }

    @PutMapping("/{id}")
    public ExamDto update(@PathVariable UUID id, @RequestBody ExamDto exam) {
        return examService.update(id, exam);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        examService.delete(id);
    }
}
