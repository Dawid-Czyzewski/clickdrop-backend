package pl.clickdrop.economy.api.dto;

import java.math.BigDecimal;

public record CollectionClaimResponse(
        String code,
        BigDecimal rewardCd,
        BigDecimal balance
) {}
