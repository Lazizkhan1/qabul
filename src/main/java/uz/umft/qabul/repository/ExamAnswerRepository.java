package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.entity.ExamAnswer;
import java.util.UUID;

public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, UUID> {
}
