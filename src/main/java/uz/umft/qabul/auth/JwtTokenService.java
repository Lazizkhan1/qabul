package uz.umft.qabul.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;
import uz.umft.qabul.config.AuthProperties;
import uz.umft.qabul.domain.entity.User;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenService {

    private static final String ISSUER = "qabul";
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TYPE = "access";
    private static final String REFRESH_TYPE = "refresh";
    private static final String REGISTRATION_TYPE = "registration";

    private final AuthProperties properties;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtTokenService(AuthProperties properties) {
        this.properties = properties;
        this.algorithm = Algorithm.HMAC256(properties.jwtSecret());
        this.verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
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

    public String createRegistrationToken(String phoneNumber) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(phoneNumber)
                .withClaim("phone_number", phoneNumber)
                .withClaim(TOKEN_TYPE_CLAIM, REGISTRATION_TYPE)
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
