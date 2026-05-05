package uz.umft.qabul.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.umft.qabul.controller.dto.response.TokenResponse;
import uz.umft.qabul.entity.Session;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.Role;
import uz.umft.qabul.enums.SessionStatus;
import uz.umft.qabul.exception.AuthException;
import uz.umft.qabul.repository.SessionRepository;
import uz.umft.qabul.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.EnumSet;

@Service
public class StaffAuthService {

    private static final Logger log = LoggerFactory.getLogger(StaffAuthService.class);
    private static final EnumSet<Role> STAFF_ROLES = EnumSet.of(Role.ADMIN, Role.MODERATOR);

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public StaffAuthService(
            UserRepository userRepository,
            SessionRepository sessionRepository,
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public TokenResponse login(String username, String password, String userAgent) {
        authenticate(username, password);

        User user = userRepository.findByPhoneNumber(username)
                .filter(candidate -> STAFF_ROLES.contains(candidate.getType()))
                .orElseThrow(() -> AuthException.unauthorized("INVALID_CREDENTIALS", "Invalid username or password"));

        log.info("Staff user logged in with username {}", username);
        return createSessionTokens(user, userAgent);
    }

    private void authenticate(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception ex) {
            throw AuthException.unauthorized("INVALID_CREDENTIALS", "Invalid username or password");
        }
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
