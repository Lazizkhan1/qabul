package uz.umft.qabul.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.umft.qabul.controller.dto.response.TokenResponse;
import uz.umft.qabul.entity.Session;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.SessionStatus;
import uz.umft.qabul.exception.AuthException;
import uz.umft.qabul.repository.SessionRepository;
import uz.umft.qabul.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final JwtTokenService jwtTokenService;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(
            JwtTokenService jwtTokenService,
            SessionRepository sessionRepository,
            UserRepository userRepository
    ) {
        this.jwtTokenService = jwtTokenService;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        DecodedJWT jwt = jwtTokenService.verifyRefreshToken(refreshToken);
        UUID userId = jwtTokenService.getUserId(jwt);

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
        String accessToken = jwtTokenService.createAccessToken(user);

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
