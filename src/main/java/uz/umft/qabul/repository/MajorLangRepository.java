package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.entity.MajorLang;
import java.util.UUID;

import java.util.Optional;

public interface MajorLangRepository extends JpaRepository<MajorLang, Integer> {
    Optional<MajorLang> findByLang(String lang);
}
