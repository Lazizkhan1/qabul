package uz.umft.qabul.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.umft.qabul.config.AuthProperties;
import uz.umft.qabul.controller.dto.request.RegisterRequest.AuthFlowResponse;
import uz.umft.qabul.controller.dto.request.RegisterRequest.VerificationResponse;
import uz.umft.qabul.controller.dto.response.TokenResponse;
import uz.umft.qabul.entity.OtpChallenge;
import uz.umft.qabul.entity.Session;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.Lang;
import uz.umft.qabul.enums.OtpChallengeStatus;
import uz.umft.qabul.enums.Role;
import uz.umft.qabul.enums.SessionStatus;
import uz.umft.qabul.exception.AuthException;
import uz.umft.qabul.integration.OtpService;
import uz.umft.qabul.repository.OtpChallengeRepository;
import uz.umft.qabul.repository.SessionRepository;
import uz.umft.qabul.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String OTP_VERIFICATION = "OTP_VERIFICATION";
    private static final String PASSWORD_LOGIN = "PASSWORD_LOGIN";
    private static final EnumSet<Role> STAFF_ROLES = EnumSet.of(Role.ADMIN, Role.MODERATOR);

    private final UserRepository userRepository;
    private final OtpChallengeRepository otpChallengeRepository;
    private final SessionRepository sessionRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final AuthProperties authProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            OtpChallengeRepository otpChallengeRepository,
            SessionRepository sessionRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            OtpService otpService,
            AuthProperties authProperties
    ) {
        this.userRepository = userRepository;
        this.otpChallengeRepository = otpChallengeRepository;
        this.sessionRepository = sessionRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.authProperties = authProperties;
    }

    @Transactional
    public TokenResponse loginApplicant(String phoneNumber, String password, String userAgent) {
        authenticate(phoneNumber, password);
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .filter(candidate -> candidate.getType() == Role.APPLICANT)
                .orElseThrow(() -> AuthException.unauthorized("INVALID_CREDENTIALS", "Invalid phone number or password"));
        log.info("Applicant logged in with phone {}", phoneNumber);
        return createSessionTokens(user, userAgent);
    }

    @Transactional
    public TokenResponse loginStaff(String username, String password, String userAgent) {
        authenticate(username, password);
        User user = userRepository.findByPhoneNumber(username)
                .filter(candidate -> STAFF_ROLES.contains(candidate.getType()))
                .orElseThrow(() -> AuthException.unauthorized("INVALID_CREDENTIALS", "Invalid username or password"));
        log.info("Staff user logged in with username {}", username);
        return createSessionTokens(user, userAgent);
    }

    @Transactional
    public AuthFlowResponse startRegistration(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            return new AuthFlowResponse(PASSWORD_LOGIN, "Applicant exists. Continue with password login.");
        }
        issueOtpChallenge(phoneNumber);
        return new AuthFlowResponse(OTP_VERIFICATION, "OTP generated.");
    }

    @Transactional
    public VerificationResponse verifyOtp(String phoneNumber, String otp) {
        verifyActiveOtp(phoneNumber, otp);
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw AuthException.conflict("DUPLICATE_PHONE", "Applicant phone already exists");
        }
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setType(Role.APPLICANT);
        user.setLang(Lang.UZ);
        user = userRepository.save(user);

        String verificationToken = jwtService.createRegistrationToken(phoneNumber, user.getId());
        return new VerificationResponse(verificationToken, authProperties.otpTtl().toSeconds());
    }

    @Transactional
    public AuthFlowResponse startPasswordReset(String phoneNumber) {
        userRepository.findByPhoneNumber(phoneNumber)
                .filter(candidate -> candidate.getType() == Role.APPLICANT)
                .orElseThrow(() -> AuthException.notFound("APPLICANT_NOT_FOUND", "Applicant not found"));
        issueOtpChallenge(phoneNumber);
        log.info("Started password reset for applicant phone {}", phoneNumber);
        return new AuthFlowResponse(OTP_VERIFICATION, "OTP generated.");
    }

    @Transactional
    public VerificationResponse verifyResetOtp(String phoneNumber, String otp) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .filter(candidate -> candidate.getType() == Role.APPLICANT)
                .orElseThrow(() -> AuthException.notFound("APPLICANT_NOT_FOUND", "Applicant not found"));
        verifyActiveOtp(phoneNumber, otp);
        String resetToken = jwtService.createPasswordResetToken(phoneNumber, user.getId());
        return new VerificationResponse(resetToken, authProperties.otpTtl().toSeconds());
    }

    @Transactional
    public TokenResponse resetPassword(String resetToken, String password, String userAgent) {
        DecodedJWT jwt = jwtService.verifyPasswordResetToken(resetToken);
        String phoneNumber = jwt.getClaim("phone_number").asString();
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw AuthException.unauthorized("INVALID_RESET_TOKEN", "Reset token is invalid");
        }

        User user = userRepository.findById(jwtService.getUserId(jwt))
                .filter(candidate -> candidate.getType() == Role.APPLICANT)
                .orElseThrow(() -> AuthException.unauthorized("INVALID_RESET_TOKEN", "Reset token is invalid"));
        if (!phoneNumber.equals(user.getPhoneNumber())) {
            throw AuthException.unauthorized("INVALID_RESET_TOKEN", "Reset token is invalid");
        }

        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);

        log.info("Completed password reset for applicant phone {}", phoneNumber);
        return createSessionTokens(user, userAgent);
    }

    @Transactional
    public TokenResponse completeRegistration(String verificationToken, String password, String userAgent) {
        DecodedJWT jwt = jwtService.verifyRegistrationToken(verificationToken);
        String phoneNumber = jwt.getClaim("phone_number").asString();
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw AuthException.unauthorized("INVALID_VERIFICATION_TOKEN", "Verification token is invalid");
        }

        User user = userRepository.findById(jwt.getClaim("user_id").as(UUID.class))
                .orElseThrow(() -> AuthException.unauthorized("INVALID_VERIFICATION_TOKEN", "Verification token is invalid"));
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);

        log.info("Completed applicant registration for phone {}", phoneNumber);
        return createSessionTokens(user, userAgent);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        DecodedJWT jwt = jwtService.verifyRefreshToken(refreshToken);
        UUID userId = jwtService.getUserId(jwt);

        Session session = sessionRepository.findByTokenAndStatus(refreshToken, SessionStatus.ACTIVE)
                .orElseThrow(() -> AuthException.unauthorized("INVALID_REFRESH_TOKEN", "Refresh token is invalid"));

        if (session.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw AuthException.unauthorized("EXPIRED_REFRESH_TOKEN", "Refresh token is expired");
        }
        if (!session.getUser().getId().equals(userId)) {
            throw AuthException.unauthorized("INVALID_REFRESH_TOKEN", "Refresh token is invalid");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> AuthException.unauthorized("INVALID_REFRESH_TOKEN", "Refresh token is invalid"));
        String accessToken = jwtService.createAccessToken(user);

        return new TokenResponse(
                accessToken,
                refreshToken,
                jwtService.accessTokenTtlSeconds(),
                jwtService.refreshTokenTtlSeconds(),
                user.getId(),
                user.getType()
        );
    }

    private void authenticate(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception ex) {
            throw AuthException.unauthorized("INVALID_CREDENTIALS", "Invalid credentials");
        }
    }

    private TokenResponse createSessionTokens(User user, String userAgent) {
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.createRefreshToken(user);

        Session session = new Session();
        session.setUser(user);
        session.setToken(refreshToken);
        session.setStatus(SessionStatus.ACTIVE);
        session.setUserAgent(userAgent);
        session.setExpiredAt(LocalDateTime.now().plusSeconds(jwtService.refreshTokenTtlSeconds()));
        sessionRepository.save(session);

        return new TokenResponse(
                accessToken,
                refreshToken,
                jwtService.accessTokenTtlSeconds(),
                jwtService.refreshTokenTtlSeconds(),
                user.getId(),
                user.getType()
        );
    }

    private void issueOtpChallenge(String phoneNumber) {
        otpChallengeRepository.findAllByPhoneNumberAndStatus(phoneNumber, OtpChallengeStatus.ACTIVE)
                .forEach(challenge -> challenge.setStatus(OtpChallengeStatus.REPLACED));

        String otp = "%06d".formatted(secureRandom.nextInt(1_000_000));
        OtpChallenge challenge = new OtpChallenge();
        challenge.setPhoneNumber(phoneNumber);
        challenge.setOtpHash(otp);
        challenge.setStatus(OtpChallengeStatus.ACTIVE);
        challenge.setExpiresAt(LocalDateTime.now().plus(authProperties.otpTtl()));
        otpChallengeRepository.save(challenge);

        otpService.sendOtp(phoneNumber, otp);

        log.info("Generated local-dev OTP for applicant phone {}: {}", phoneNumber, otp);
    }

    private OtpChallenge verifyActiveOtp(String phoneNumber, String otp) {
        OtpChallenge challenge = otpChallengeRepository
                .findFirstByPhoneNumberAndStatusOrderByCreatedAtDesc(phoneNumber, OtpChallengeStatus.ACTIVE)
                .orElseThrow(() -> AuthException.unauthorized("INVALID_OTP", "OTP is invalid"));

        if (challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            challenge.setStatus(OtpChallengeStatus.EXPIRED);
            throw AuthException.unauthorized("EXPIRED_OTP", "OTP is expired");
        }
        if (!Objects.equals(otp, challenge.getOtpHash())) {
            throw AuthException.unauthorized("INVALID_OTP", "OTP is invalid");
        }

        challenge.setStatus(OtpChallengeStatus.VERIFIED);
        challenge.setVerifiedAt(LocalDateTime.now());
        return challenge;
    }
}
