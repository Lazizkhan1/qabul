package uz.umft.qabul.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.umft.qabul.auth.dto.ApplicantRegistrationDtos.AuthFlowResponse;
import uz.umft.qabul.auth.dto.ApplicantRegistrationDtos.VerificationResponse;
import uz.umft.qabul.auth.dto.TokenResponse;
import uz.umft.qabul.config.AuthProperties;
import uz.umft.qabul.domain.entity.OtpChallenge;
import uz.umft.qabul.domain.entity.Session;
import uz.umft.qabul.domain.entity.User;
import uz.umft.qabul.domain.enums.Lang;
import uz.umft.qabul.domain.enums.OtpChallengeStatus;
import uz.umft.qabul.domain.enums.Role;
import uz.umft.qabul.domain.enums.SessionStatus;
import uz.umft.qabul.repository.OtpChallengeRepository;
import uz.umft.qabul.repository.SessionRepository;
import uz.umft.qabul.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class ApplicantRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicantRegistrationService.class);
    private static final String OTP_VERIFICATION = "OTP_VERIFICATION";
    private static final String PASSWORD_LOGIN = "PASSWORD_LOGIN";

    private final UserRepository userRepository;
    private final OtpChallengeRepository otpChallengeRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthProperties authProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApplicantRegistrationService(
            UserRepository userRepository,
            OtpChallengeRepository otpChallengeRepository,
            SessionRepository sessionRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            AuthProperties authProperties
    ) {
        this.userRepository = userRepository;
        this.otpChallengeRepository = otpChallengeRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.authProperties = authProperties;
    }

    @Transactional
    public AuthFlowResponse start(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            return new AuthFlowResponse(PASSWORD_LOGIN, "Applicant exists. Continue with password login.");
        }

        otpChallengeRepository.findAllByPhoneNumberAndStatus(phoneNumber, OtpChallengeStatus.ACTIVE)
                .forEach(challenge -> challenge.setStatus(OtpChallengeStatus.REPLACED));

        String otp = "%06d".formatted(secureRandom.nextInt(1_000_000));
        OtpChallenge challenge = new OtpChallenge();
        challenge.setPhoneNumber(phoneNumber);
        challenge.setOtpHash(passwordEncoder.encode(otp));
        challenge.setStatus(OtpChallengeStatus.ACTIVE);
        challenge.setExpiresAt(LocalDateTime.now().plus(authProperties.otpTtl()));
        otpChallengeRepository.save(challenge);

        log.info("Generated local-dev OTP for applicant phone {}: {}", phoneNumber, otp);
        return new AuthFlowResponse(OTP_VERIFICATION, "OTP generated.");
    }

    @Transactional
    public VerificationResponse verifyOtp(String phoneNumber, String otp) {
        OtpChallenge challenge = otpChallengeRepository
                .findFirstByPhoneNumberAndStatusOrderByCreatedAtDesc(phoneNumber, OtpChallengeStatus.ACTIVE)
                .orElseThrow(() -> AuthException.unauthorized("INVALID_OTP", "OTP is invalid"));

        if (challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            challenge.setStatus(OtpChallengeStatus.EXPIRED);
            throw AuthException.unauthorized("EXPIRED_OTP", "OTP is expired");
        }
        if (!passwordEncoder.matches(otp, challenge.getOtpHash())) {
            throw AuthException.unauthorized("INVALID_OTP", "OTP is invalid");
        }

        challenge.setStatus(OtpChallengeStatus.VERIFIED);
        challenge.setVerifiedAt(LocalDateTime.now());
        String verificationToken = jwtTokenService.createRegistrationToken(phoneNumber);
        return new VerificationResponse(verificationToken, authProperties.otpTtl().toSeconds());
    }

    @Transactional
    public TokenResponse setPassword(String verificationToken, String password, String userAgent) {
        DecodedJWT jwt = jwtTokenService.verifyRegistrationToken(verificationToken);
        String phoneNumber = jwt.getClaim("phone_number").asString();
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw AuthException.unauthorized("INVALID_VERIFICATION_TOKEN", "Verification token is invalid");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw AuthException.conflict("DUPLICATE_PHONE", "Applicant phone already exists");
        }

        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setType(Role.APPLICANT);
        user.setLang(Lang.UZ);
        user = userRepository.save(user);

        log.info("Completed applicant registration for phone {}", phoneNumber);
        return createSessionTokens(user, userAgent);
    }

    private TokenResponse createSessionTokens(User user, String userAgent) {
        String accessToken = jwtTokenService.createAccessToken(user);
        String refreshToken = jwtTokenService.createRefreshToken(user);

        Session session = new Session();
        session.setUser(user);
        session.setToken(refreshToken);
        session.setStatus(SessionStatus.ACTIVE);
        session.setUserAgent(userAgent);
        session.setExpiredAt(LocalDateTime.now().plusSeconds(jwtTokenService.refreshTokenTtlSeconds()));
        sessionRepository.save(session);

        return new TokenResponse(
                accessToken,
                refreshToken,
                jwtTokenService.accessTokenTtlSeconds(),
                jwtTokenService.refreshTokenTtlSeconds(),
                user.getId(),
                user.getType()
        );
    }
}
