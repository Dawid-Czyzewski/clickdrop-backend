package pl.clickdrop.economy.api.dto;

import java.math.BigDecimal;

public record CollectionProgressResponse(
        String code,
        String displayName,
        int sortOrder,
        int owned,
        int total,
        boolean completed,
        BigDecimal rewardCd,
        boolean rewardClaimed
) {}
