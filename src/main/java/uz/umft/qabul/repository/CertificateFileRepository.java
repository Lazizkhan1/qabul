package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.entity.CertificateFile;

import java.util.UUID;

public interface CertificateFileRepository extends JpaRepository<CertificateFile, UUID> {
}
