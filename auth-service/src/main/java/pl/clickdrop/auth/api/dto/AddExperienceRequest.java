package pl.clickdrop.auth.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddExperienceRequest(
        @NotNull @Min(1) Integer amount
) {
}
