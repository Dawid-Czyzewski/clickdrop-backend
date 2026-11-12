package pl.clickdrop.economy.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record CollectionDetailResponse(
        String code,
        String displayName,
        int sortOrder,
        int owned,
        int total,
        boolean completed,
        BigDecimal rewardCd,
        boolean rewardClaimed,
        List<CollectionItemProgressDto> items
) {}
