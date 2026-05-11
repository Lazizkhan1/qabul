package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.umft.qabul.entity.Tuition;

import java.util.List;
import java.util.UUID;

public interface TuitionRepository extends JpaRepository<Tuition, UUID> {
    @Query("SELECT t FROM Tuition t INNER JOIN t.majorType m INNER JOIN t.majorLang m_0 WHERE m.type = :majorType AND m_0.lang = :majorLang")
    List<Tuition> findAllByMajorType_TypeAndMajorLang_Lang(String majorType, String majorLang);
    List<Tuition> findAllByMajorType_Type(String majorType);
    List<Tuition> findAllByMajorLang_Lang(String majorLang);
}
