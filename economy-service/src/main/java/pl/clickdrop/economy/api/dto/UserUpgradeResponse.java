package pl.clickdrop.economy.api.dto;

public record UserUpgradeResponse(
        Long upgradeId,
        String code,
        Integer level
) {
}
