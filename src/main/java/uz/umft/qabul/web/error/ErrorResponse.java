package uz.umft.qabul.web.error;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(
        String code,
        String message,
        Map<String, Object> details,
        OffsetDateTime timestamp
) {
}
