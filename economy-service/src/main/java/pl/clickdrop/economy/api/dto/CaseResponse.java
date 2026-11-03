package pl.clickdrop.economy.api.dto;

import java.math.BigDecimal;

public record CaseResponse(
        Long id,
        String code,
        String displayName,
        BigDecimal price,
        Integer sortOrder,
        Integer minLevel,
        Integer maxOpenCount,
        Integer openX3MinLevel,
        Integer openX5MinLevel
) {
}
