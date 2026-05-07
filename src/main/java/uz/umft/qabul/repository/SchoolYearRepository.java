package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.entity.SchoolYear;
import java.util.UUID;

import java.util.Optional;

public interface SchoolYearRepository extends JpaRepository<SchoolYear, Integer> {
    Optional<SchoolYear> findByTitle(String title);
}
