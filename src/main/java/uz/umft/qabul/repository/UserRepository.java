package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.domain.entity.User;
import uz.umft.qabul.domain.enums.Role;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByUsername(String username);

    Optional<User> findByType(Role type);

    boolean existsByPhoneNumber(String phoneNumber);
}
