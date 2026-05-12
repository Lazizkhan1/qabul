package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.entity.Contract;

import java.util.Optional;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> {
    Optional<Contract> findByExamSessionId(UUID examSessionId);
}
