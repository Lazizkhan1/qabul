package uz.umft.qabul.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.Role;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByType(Role type);

    Page<User> findByType(Role type, Pageable pageable);

    boolean existsByPhoneNumber(String phoneNumber);
}
