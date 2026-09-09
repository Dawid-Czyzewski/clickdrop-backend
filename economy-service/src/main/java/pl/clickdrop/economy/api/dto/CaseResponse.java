package pl.clickdrop.economy.api.dto;


import java.math.BigDecimal;

public record CaseResponse(Long id, String code, BigDecimal price, Integer sortOrder) {
}
