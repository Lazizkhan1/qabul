package uz.umft.qabul.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.umft.qabul.dto.exam.ExamAnswerDto;
import uz.umft.qabul.dto.exam.ExamQuestionDto;
import uz.umft.qabul.entity.ExamAnswer;
import uz.umft.qabul.entity.ExamQuestion;
import uz.umft.qabul.repository.ExamAnswerRepository;
import uz.umft.qabul.repository.ExamQuestionRepository;
import uz.umft.qabul.repository.SubjectRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamManagementService {

    private final ExamQuestionRepository questionRepository;
    private final ExamAnswerRepository answerRepository;
    private final SubjectRepository subjectRepository;

    public List<ExamQuestionDto> listQuestions() {
        return questionRepository.findAll().stream().map(this::toQuestionDto).collect(Collectors.toList());
    }

    @Transactional
    public ExamQuestionDto createQuestion(ExamQuestionDto dto) {
        ExamQuestion question = new ExamQuestion();
        mapToQuestionEntity(dto, question);
        return toQuestionDto(questionRepository.save(question));
    }

    @Transactional
    public ExamQuestionDto updateQuestion(Integer id, ExamQuestionDto dto) {
        ExamQuestion question = questionRepository.findById(id).orElseThrow();
        mapToQuestionEntity(dto, question);
        return toQuestionDto(questionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Integer id) {
        questionRepository.deleteById(id);
    }

    public List<ExamAnswerDto> listAnswers() {
        return answerRepository.findAll().stream().map(this::toAnswerDto).collect(Collectors.toList());
    }

    @Transactional
    public ExamAnswerDto createAnswer(ExamAnswerDto dto) {
        ExamAnswer answer = new ExamAnswer();
        mapToAnswerEntity(dto, answer);
        return toAnswerDto(answerRepository.save(answer));
    }

    @Transactional
    public ExamAnswerDto updateAnswer(Integer id, ExamAnswerDto dto) {
        ExamAnswer answer = answerRepository.findById(id).orElseThrow();
        mapToAnswerEntity(dto, answer);
        return toAnswerDto(answerRepository.save(answer));
    }

    @Transactional
    public void deleteAnswer(Integer id) {
        answerRepository.deleteById(id);
    }

    private ExamQuestionDto toQuestionDto(ExamQuestion q) {
        return ExamQuestionDto.builder()
                .id(q.getId())
                .questionText(q.getQuestionText())
                .point(q.getPoint())
                .subjectId(q.getSubject() != null ? q.getSubject().getId() : null)
                .build();
    }

    private void mapToQuestionEntity(ExamQuestionDto dto, ExamQuestion q) {
        q.setQuestionText(dto.getQuestionText());
        q.setPoint(dto.getPoint());
        if (dto.getSubjectId() != null) {
            q.setSubject(subjectRepository.findById(dto.getSubjectId()).orElseThrow());
        }
    }

    private ExamAnswerDto toAnswerDto(ExamAnswer a) {
        return ExamAnswerDto.builder()
                .id(a.getId())
                .questionId(a.getQuestion() != null ? a.getQuestion().getId() : null)
                .isCorrect(a.getIsCorrect())
                .answerText(a.getAnswerText())
                .build();
    }

    private void mapToAnswerEntity(ExamAnswerDto dto, ExamAnswer a) {
        a.setIsCorrect(dto.getIsCorrect());
        a.setAnswerText(dto.getAnswerText());
        if (dto.getQuestionId() != null) {
            a.setQuestion(questionRepository.findById(dto.getQuestionId()).orElseThrow());
        }
    }

    @Transactional
    public uz.umft.qabul.dto.exam.ImportSummaryDto importQuestionsFromDocx(org.springframework.web.multipart.MultipartFile file, Integer subjectId) {
        uz.umft.qabul.entity.Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        int questionsImported = 0;
        try (org.apache.poi.xwpf.usermodel.XWPFDocument doc = new org.apache.poi.xwpf.usermodel.XWPFDocument(file.getInputStream())) {
            List<String> lines = doc.getParagraphs().stream()
                    .map(org.apache.poi.xwpf.usermodel.XWPFParagraph::getText)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            for (int i = 0; i <= lines.size() - 5; i += 5) {
                String qText = lines.get(i);
                String ansA = lines.get(i + 1);
                String ansB = lines.get(i + 2);
                String ansC = lines.get(i + 3);
                String ansD = lines.get(i + 4);

                ExamQuestion question = new ExamQuestion();
                question.setQuestionText(qText);
                question.setSubject(subject);
                question.setPoint(1.0); // Default point
                question = questionRepository.save(question);

                saveAnswer(question, ansA, true);
                saveAnswer(question, ansB, false);
                saveAnswer(question, ansC, false);
                saveAnswer(question, ansD, false);

                questionsImported++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse DOCX file", e);
        }

        return new uz.umft.qabul.dto.exam.ImportSummaryDto(questionsImported);
    }

    private void saveAnswer(ExamQuestion question, String text, boolean isCorrect) {
        ExamAnswer answer = new ExamAnswer();
        answer.setQuestion(question);
        answer.setAnswerText(text);
        answer.setIsCorrect(isCorrect);
        answerRepository.save(answer);
    }
}
