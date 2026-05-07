package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.entity.BachelorCert;
import java.util.UUID;

public interface BachelorCertRepository extends JpaRepository<BachelorCert, UUID> {
}
