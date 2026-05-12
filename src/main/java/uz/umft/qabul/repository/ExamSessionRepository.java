package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.umft.qabul.entity.ExamSession;
import uz.umft.qabul.enums.ExamSessionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, UUID> {
    Optional<ExamSession> findByApplicationId(UUID applicationId);

    List<ExamSession> findByStatusAndExpiresAtBefore(ExamSessionStatus status, LocalDateTime time);
}
