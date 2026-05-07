package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.entity.Exam;
import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID> {
}
