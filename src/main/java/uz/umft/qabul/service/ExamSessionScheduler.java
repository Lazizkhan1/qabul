package uz.umft.qabul.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.umft.qabul.dto.exam.SubmitAnswersRequest;
import uz.umft.qabul.entity.ExamSession;
import uz.umft.qabul.enums.ExamSessionStatus;
import uz.umft.qabul.repository.ExamSessionRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExamSessionScheduler {

    private final ExamSessionRepository examSessionRepository;
    private final ExamService examService;

    @Scheduled(cron = "0 * * * * *") // Every minute
    public void closeExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<ExamSession> expiredSessions = examSessionRepository.findByStatusAndExpiresAtBefore(ExamSessionStatus.ONGOING, now);

        for (ExamSession session : expiredSessions) {
            log.info("Closing expired session: {}", session.getId());
            try {
                SubmitAnswersRequest request = new SubmitAnswersRequest();
                request.setAnswers(new HashMap<>()); // Pass empty answers or handle properly in completeSession if no answers are submitted
                examService.completeSession(session.getId(), request);
            } catch (Exception e) {
                log.error("Failed to close expired session: {}", session.getId(), e);
            }
        }
    }
}
