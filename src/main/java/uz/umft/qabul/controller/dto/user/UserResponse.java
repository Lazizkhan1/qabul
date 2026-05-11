package uz.umft.qabul.controller.dto.user;

import uz.umft.qabul.enums.Lang;
import uz.umft.qabul.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String phoneNumber,
        Role type,
        Lang lang,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
