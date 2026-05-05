package uz.umft.qabul.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class StaffLoginDtos {

    private StaffLoginDtos() {
    }

    public record StaffLoginRequest(
            @NotBlank
            @Size(min = 3, max = 100)
            String username,

            @NotBlank
            String password
    ) {
    }
}
