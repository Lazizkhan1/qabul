package uz.umft.qabul.controller.dto.user;

import jakarta.validation.constraints.NotBlank;
import uz.umft.qabul.enums.Lang;

public record CreateModeratorRequest(
        @NotBlank String username,
        @NotBlank String password,
        Lang lang
) {
}
