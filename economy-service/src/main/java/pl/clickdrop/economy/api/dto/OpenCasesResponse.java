package pl.clickdrop.economy.api.dto;


import java.math.BigDecimal;
import java.util.List;

public record OpenCasesResponse(
        int count,
        BigDecimal totalCost,
        BigDecimal balance,
        List<OpenedCaseDropResponse> drops,
        Integer experienceGained,
        Integer level,
        Integer experience
) {
}
