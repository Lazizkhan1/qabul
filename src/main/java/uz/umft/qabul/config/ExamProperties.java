package uz.umft.qabul.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "qabul.exam")
@Getter
@Setter
public class ExamProperties {
    private Integer durationMinutes = 60;
}
