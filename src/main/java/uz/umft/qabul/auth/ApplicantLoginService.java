package uz.umft.qabul.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.umft.qabul.auth.dto.TokenResponse;
import uz.umft.qabul.domain.entity.Session;
import uz.umft.qabul.domain.entity.User;
import uz.umft.qabul.domain.enums.Role;
import uz.umft.qabul.domain.enums.SessionStatus;
import uz.umft.qabul.repository.SessionRepository;
import uz.umft.qabul.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class ApplicantLoginService {

    private static final Logger log = LoggerFactory.getLogger(ApplicantLoginService.class);

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public ApplicantLoginService(
            UserRepository userRepository,
            SessionRepository sessionRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public TokenResponse login(String phoneNumber, String password, String userAgent) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .filter(candidate -> candidate.getType() == Role.APPLICANT)
                .orElseThrow(() -> AuthException.unauthorized("INVALID_CREDENTIALS", "Invalid phone number or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw AuthException.unauthorized("INVALID_CREDENTIALS", "Invalid phone number or password");
        }

        log.info("Applicant logged in with phone {}", phoneNumber);
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
