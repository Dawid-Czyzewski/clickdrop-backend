package pl.clickdrop.auth.api.dto;

import java.math.BigDecimal;

public record UserResponse(
        String username,
        String email,
        Integer level,
        Integer experience,
        BigDecimal clickDollars
) {
}
