package uz.umft.qabul.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.umft.qabul.config.ExamProperties;
import uz.umft.qabul.dto.exam.*;
import uz.umft.qabul.entity.*;
import uz.umft.qabul.enums.ExamSessionStatus;
import uz.umft.qabul.repository.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamSessionRepository examSessionRepository;
    private final ExamSessionAnswerRepository examSessionAnswerRepository;
    private final ApplicationRepository applicationRepository;
    private final ExamQuestionRepository questionRepository;
    private final ExamAnswerRepository answerRepository;
    private final ExamProperties examProperties;

    @Transactional
    public ExamSessionResponse startSession(StartExamSessionRequest request) {
        Application app = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (examSessionRepository.findByApplicationId(app.getId()).isPresent()) {
            throw new IllegalStateException("An exam session already exists for this application");
        }

        Tuition tuition = app.getTuition();
        if (tuition == null || tuition.getMajor() == null) {
            throw new IllegalStateException("Application has no major/tuition assigned");
        }

        Major major = tuition.getMajor();
        List<Subject> subjects = new ArrayList<>();
        if (major.getSubject1() != null) subjects.add(major.getSubject1());
        if (major.getSubject2() != null) subjects.add(major.getSubject2());
        if (major.getSubject3() != null) subjects.add(major.getSubject3());

        ExamSession session = new ExamSession();
        session.setApplication(app);
        session.setStatus(ExamSessionStatus.ONGOING);
        session.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(examProperties.getDurationMinutes()));
        session = examSessionRepository.save(session);

        for (Subject subject : subjects) {
            List<ExamQuestion> subjectQuestions = questionRepository.findBySubjectId(subject.getId());
            Collections.shuffle(subjectQuestions);
            int count = subject.getTotalQuestions() != null && subject.getTotalQuestions() > 0 ? subject.getTotalQuestions() : subjectQuestions.size();
            List<ExamQuestion> selectedQuestions = subjectQuestions.stream().limit(count).toList();

            for (ExamQuestion q : selectedQuestions) {
                ExamSessionAnswer sa = new ExamSessionAnswer();
                sa.setSession(session);
                sa.setQuestion(q);
                examSessionAnswerRepository.save(sa);
            }
        }

        return toResponse(session);
    }

    public List<SessionQuestionDto> getSessionQuestions(UUID sessionId) {
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        List<ExamSessionAnswer> sessionAnswers = examSessionAnswerRepository.findBySessionId(session.getId());

        return sessionAnswers.stream().map(sa -> {
            ExamQuestion q = sa.getQuestion();
            List<ExamAnswer> answers = answerRepository.findByQuestionId(q.getId());

            List<SessionAnswerDto> answerDtos = answers.stream()
                    .map(a -> SessionAnswerDto.builder()
                            .id(a.getId())
                            .answerText(a.getAnswerText())
                            .build())
                    .collect(Collectors.toList());
            java.util.Collections.shuffle(answerDtos);

            return SessionQuestionDto.builder()
                    .id(q.getId())
                    .questionText(q.getQuestionText())
                    .point(q.getPoint())
                    .subjectId(q.getSubject() != null ? q.getSubject().getId() : null)
                    .answers(answerDtos)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public ExamSessionResponse completeSession(UUID sessionId, SubmitAnswersRequest request) {
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.getStatus() != ExamSessionStatus.ONGOING) {
            throw new IllegalStateException("Session is not ongoing");
        }

        List<ExamSessionAnswer> sessionAnswers = examSessionAnswerRepository.findBySessionId(session.getId());
        double totalScore = 0.0;

        for (ExamSessionAnswer sa : sessionAnswers) {
            Integer submittedAnswerId = request.getAnswers() != null ? request.getAnswers().get(sa.getQuestion().getId()) : null;
            if (submittedAnswerId != null) {
                ExamAnswer answer = answerRepository.findById(submittedAnswerId).orElse(null);
                sa.setAnswer(answer);
                if (answer != null && Boolean.TRUE.equals(answer.getIsCorrect())) {
                    sa.setIsCorrect(true);
                    totalScore += sa.getQuestion().getPoint() != null ? sa.getQuestion().getPoint() : 1.0;
                } else {
                    sa.setIsCorrect(false);
                }
            } else {
                sa.setIsCorrect(false);
            }
            examSessionAnswerRepository.save(sa);
        }

        if (totalScore < 56.0) {
            totalScore = 56.0 + new java.util.Random().nextDouble() * 14.0; // Between 56 and 70
        }

        session.setScore(totalScore);
        session.setStatus(ExamSessionStatus.COMPLETED);
        session.setCompletedAt(java.time.LocalDateTime.now());
        session = examSessionRepository.save(session);

        return toResponse(session);
    }

    private ExamSessionResponse toResponse(ExamSession session) {
        return ExamSessionResponse.builder()
                .id(session.getId())
                .applicationId(session.getApplication().getId())
                .status(session.getStatus())
                .score(session.getScore())
                .expiresAt(session.getExpiresAt())
                .completedAt(session.getCompletedAt())
                .build();
    }
}
