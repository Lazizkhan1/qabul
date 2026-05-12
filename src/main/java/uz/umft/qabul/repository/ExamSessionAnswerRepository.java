package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.umft.qabul.entity.ExamSessionAnswer;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExamSessionAnswerRepository extends JpaRepository<ExamSessionAnswer, UUID> {
    List<ExamSessionAnswer> findBySessionId(UUID sessionId);
}
