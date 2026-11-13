package pl.clickdrop.economy.api.dto;

import pl.clickdrop.economy.domain.battle.BattleStatus;

import java.math.BigDecimal;
import java.util.List;

public record BattleResponse(
        Long id,
        Long caseId,
        int rounds,
        BattleStatus status,
        Long player1UserId,
        Long player2UserId,
        BigDecimal player1TotalValue,
        BigDecimal player2TotalValue,
        Long winnerUserId,
        BigDecimal entryCost,
        List<BattleDropDto> roundsDrops
) {}
