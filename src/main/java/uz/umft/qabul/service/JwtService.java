package uz.umft.qabul.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;
import uz.umft.qabul.config.AuthProperties;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.exception.AuthException;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final String ISSUER = "qabul";
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TYPE = "access";
    private static final String REFRESH_TYPE = "refresh";
    private static final String REGISTRATION_TYPE = "registration";
    private static final String RESET_TYPE = "password_reset";
    private static final String DOWNLOAD_TYPE = "download";

    private final AuthProperties properties;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService(AuthProperties properties) {
        this.properties = properties;
        this.algorithm = Algorithm.HMAC256(properties.jwtSecret());
        this.verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
    }

    public String createDownloadToken(String fileId, String userId) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(fileId)
                .withClaim("file_id", fileId)
                .withClaim("user_id", userId)
                .withClaim(TOKEN_TYPE_CLAIM, DOWNLOAD_TYPE)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(3600))) // 1 hour
                .sign(algorithm);
    }

    public String verifyDownloadToken(String token) {
        DecodedJWT jwt = verify(token);
        if (!DOWNLOAD_TYPE.equals(jwt.getClaim(TOKEN_TYPE_CLAIM).asString())) {
            throw AuthException.unauthorized("INVALID_DOWNLOAD_TOKEN", "Download token is invalid");
        }
        return jwt.getClaim("file_id").asString();
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(user.getId().toString())
                .withClaim("user_id", user.getId().toString())
                .withClaim("role", user.getType().name())
                .withClaim(TOKEN_TYPE_CLAIM, ACCESS_TYPE)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(properties.accessTokenTtl())))
                .sign(algorithm);
    }

    public String createRefreshToken(User user) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(user.getId().toString())
                .withClaim("user_id", user.getId().toString())
                .withClaim("role", user.getType().name())
                .withClaim(TOKEN_TYPE_CLAIM, REFRESH_TYPE)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(properties.refreshTokenTtl())))
                .sign(algorithm);
    }

    public String createRegistrationToken(String phoneNumber, UUID userId) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(phoneNumber)
                .withClaim("user_id", userId.toString())
                .withClaim("phone_number", phoneNumber)
                .withClaim(TOKEN_TYPE_CLAIM, REGISTRATION_TYPE)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(properties.otpTtl())))
                .sign(algorithm);
    }

    public String createPasswordResetToken(String phoneNumber, UUID userId) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(phoneNumber)
                .withClaim("user_id", userId.toString())
                .withClaim("phone_number", phoneNumber)
                .withClaim(TOKEN_TYPE_CLAIM, RESET_TYPE)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(properties.otpTtl())))
                .sign(algorithm);
    }

    public DecodedJWT verifyRefreshToken(String token) {
        DecodedJWT jwt = verify(token);
        if (!REFRESH_TYPE.equals(jwt.getClaim(TOKEN_TYPE_CLAIM).asString())) {
            throw AuthException.unauthorized("INVALID_REFRESH_TOKEN", "Refresh token is invalid");
        }
        return jwt;
    }

    public DecodedJWT decodeJWT(String token, String uri) {
        if (uri.equals("/api/v1/auth/applicant/set-password")) {
            return verifyRegistrationToken(token);
        } else if (uri.equals("/api/v1/auth/applicant/reset/set-password")) {
            return verifyPasswordResetToken(token);
        } else if (uri.startsWith("/api/v1/files/")) {
            DecodedJWT jwt = verify(token);
            String type = jwt.getClaim(TOKEN_TYPE_CLAIM).asString();
            if (DOWNLOAD_TYPE.equals(type) || ACCESS_TYPE.equals(type)) {
                return jwt;
            }
            throw AuthException.unauthorized("INVALID_TOKEN", "Invalid token type for file access");
        } else {
            return verifyAccessToken(token);
        }
    }

    public DecodedJWT verifyAccessToken(String token) {
        DecodedJWT jwt = verify(token);
        if (!ACCESS_TYPE.equals(jwt.getClaim(TOKEN_TYPE_CLAIM).asString())) {
            throw AuthException.unauthorized("INVALID_ACCESS_TOKEN", "Access token is invalid");
        }
        return jwt;
    }

    public DecodedJWT verifyRegistrationToken(String token) {
        DecodedJWT jwt = verify(token);
        if (!REGISTRATION_TYPE.equals(jwt.getClaim(TOKEN_TYPE_CLAIM).asString())) {
            throw AuthException.unauthorized("INVALID_VERIFICATION_TOKEN", "Verification token is invalid");
        }
        return jwt;
    }

    public DecodedJWT verifyPasswordResetToken(String token) {
        DecodedJWT jwt = verify(token);
        if (!RESET_TYPE.equals(jwt.getClaim(TOKEN_TYPE_CLAIM).asString())) {
            throw AuthException.unauthorized("INVALID_RESET_TOKEN", "Reset token is invalid");
        }
        return jwt;
    }

    public UUID getUserId(DecodedJWT jwt) {
        return UUID.fromString(jwt.getClaim("user_id").asString());
    }

    public long accessTokenTtlSeconds() {
        return properties.accessTokenTtl().toSeconds();
    }

    public long refreshTokenTtlSeconds() {
        return properties.refreshTokenTtl().toSeconds();
    }

    private DecodedJWT verify(String token) {
        try {
            return verifier.verify(token);
        } catch (JWTVerificationException ex) {
            throw AuthException.unauthorized("INVALID_TOKEN", "Token is invalid or expired");
        }
    }
}
