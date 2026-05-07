package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.entity.Contract;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> {
}
