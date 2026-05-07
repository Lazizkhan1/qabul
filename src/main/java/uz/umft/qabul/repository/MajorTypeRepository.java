package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.entity.MajorType;
import java.util.UUID;

import java.util.Optional;

public interface MajorTypeRepository extends JpaRepository<MajorType, Integer> {
    Optional<MajorType> findByType(String type);
}
