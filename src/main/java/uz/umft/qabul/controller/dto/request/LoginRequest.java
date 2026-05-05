package uz.umft.qabul.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public record LoginRequest(
            @JsonProperty("phone_number")
            @NotBlank
            @Pattern(regexp = "^[0-9]{9,15}$")
            String phoneNumber,

            @NotBlank
            String password
    ) {

}
