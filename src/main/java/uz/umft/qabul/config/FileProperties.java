package uz.umft.qabul.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "qabul.files")
public record FileProperties(
        @NotBlank String baseDir,
        @Min(1) long maxSizeBytes,
        @NotEmpty List<String> allowedContentTypes,
        @NotEmpty List<String> allowedExtensions
) {
}
