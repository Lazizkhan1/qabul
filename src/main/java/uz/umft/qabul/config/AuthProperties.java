package uz.umft.qabul.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "qabul.auth")
public record AuthProperties(
        @NotBlank String jwtSecret,
        @NotNull Duration accessTokenTtl,
        @NotNull Duration refreshTokenTtl,
        @NotNull Duration otpTtl,
        @NotBlank String adminUsername,
        @NotBlank String adminPassword
) {
}
