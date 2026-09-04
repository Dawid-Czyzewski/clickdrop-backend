package pl.clickdrop.economy.api.dto;

import java.math.BigDecimal;

public record WalletResponse(
        Long id,
        Long userId,
        BigDecimal balance
) {
}
