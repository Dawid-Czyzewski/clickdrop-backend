package pl.clickdrop.economy.api.dto;

public record CreateBattleRequest(
        Long caseId,
        Integer rounds,
        Boolean vsBot
) {}
