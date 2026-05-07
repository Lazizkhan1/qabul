package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.entity.Tuition;

import java.util.UUID;

public interface TuitionRepository extends JpaRepository<Tuition, UUID> {
}
