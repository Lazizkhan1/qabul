package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.entity.Cert;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CertRepository extends JpaRepository<Cert, UUID> {

    List<Cert> findByApplication_IdIn(Collection<UUID> applicationIds);
}
