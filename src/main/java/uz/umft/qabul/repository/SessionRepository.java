package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.domain.entity.Session;
import uz.umft.qabul.domain.entity.User;
import uz.umft.qabul.domain.enums.SessionStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByTokenAndStatus(String token, SessionStatus status);

    List<Session> findAllByUserAndStatus(User user, SessionStatus status);
}
