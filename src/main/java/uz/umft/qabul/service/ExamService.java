package uz.umft.qabul.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.umft.qabul.dto.exam.ExamDto;
import uz.umft.qabul.entity.Exam;
import uz.umft.qabul.entity.Tuition;
import uz.umft.qabul.repository.ApplicationRepository;
import uz.umft.qabul.repository.ExamRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final ApplicationRepository applicationRepository;

    public List<ExamDto> findAll() {
        return examRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public ExamDto findById(UUID id) {
        return examRepository.findById(id).map(this::toDto).orElseThrow();
    }

    @Transactional
    public ExamDto create(ExamDto dto) {
        Exam exam = new Exam();
        mapToEntity(dto, exam);
        return toDto(examRepository.save(exam));
    }

    @Transactional
    public ExamDto update(UUID id, ExamDto dto) {
        Exam exam = examRepository.findById(id).orElseThrow();
        mapToEntity(dto, exam);
        return toDto(examRepository.save(exam));
    }

    @Transactional
    public void delete(UUID id) {
        examRepository.deleteById(id);
    }

    public String createExamQuestions(Tuition tuition) {
        return "";
    }

    private ExamDto toDto(Exam e) {
        return ExamDto.builder()
                .id(e.getId())
                .applicationId(e.getApplication() != null ? e.getApplication().getId() : null)
                .status(e.getStatus())
                .duration(e.getDuration())
                .score(e.getScore())
                .completedAt(e.getCompletedAt())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private void mapToEntity(ExamDto dto, Exam e) {
        e.setStatus(dto.getStatus());
        e.setDuration(dto.getDuration());
        e.setScore(dto.getScore());
        e.setCompletedAt(dto.getCompletedAt());
        if (dto.getApplicationId() != null) {
            e.setApplication(applicationRepository.findById(dto.getApplicationId()).orElseThrow());
        }
    }
}
