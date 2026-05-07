package uz.umft.qabul.controller.dto.application;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class AutoAcceptToggleDto {

    public record Request(@NotNull Boolean enabled) {
    }

    public record Response(boolean enabled, LocalDateTime updatedAt) {
    }
}
