package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.entity.CertCategory;

public interface CertCategoryRepository extends JpaRepository<CertCategory, Integer> {
}
