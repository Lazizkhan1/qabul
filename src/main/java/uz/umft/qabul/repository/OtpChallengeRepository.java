package uz.umft.qabul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.umft.qabul.entity.OtpChallenge;
import uz.umft.qabul.enums.OtpChallengeStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, UUID> {

    List<OtpChallenge> findAllByPhoneNumberAndStatus(String phoneNumber, OtpChallengeStatus status);

    Optional<OtpChallenge> findFirstByPhoneNumberAndStatusOrderByCreatedAtDesc(String phoneNumber, OtpChallengeStatus status);
}
