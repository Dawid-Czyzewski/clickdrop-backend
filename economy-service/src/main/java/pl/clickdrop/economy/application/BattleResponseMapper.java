package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.clickdrop.economy.api.dto.BattleDropDto;
import pl.clickdrop.economy.api.dto.BattleResponse;
import pl.clickdrop.economy.domain.battle.Battle;
import pl.clickdrop.economy.domain.battle.BattleRoundDrop;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BattleResponseMapper {

    private final ItemRepository itemRepository;

    public BattleResponse toResponse(Battle battle, List<BattleRoundDrop> drops) {
        Map<Long, Item> itemsById = itemRepository.findAllById(
                drops.stream().map(BattleRoundDrop::getItemId).distinct().toList()
        ).stream().collect(Collectors.toMap(Item::getId, Function.identity()));

        List<BattleDropDto> dtos = drops.stream().map(drop -> {
            Item item = itemsById.get(drop.getItemId());
            return new BattleDropDto(
                    drop.getRoundIndex(),
                    drop.getSide(),
                    drop.getItemId(),
                    item != null ? item.getCode() : "",
                    item != null ? item.getDisplayName() : "",
                    item != null ? item.getRarity() : null,
                    drop.getExterior(),
                    drop.isStatTrack(),
                    drop.getFloatValue(),
                    drop.getPaintSeed(),
                    drop.getDropValue(),
                    drop.getInventoryItemId()
            );
        }).toList();

        return new BattleResponse(
                battle.getId(),
                battle.getCaseId(),
                battle.getRounds(),
                battle.getStatus(),
                battle.getPlayer1UserId(),
                battle.getPlayer2UserId(),
                battle.getPlayer1TotalValue(),
                battle.getPlayer2TotalValue(),
                battle.getWinnerUserId(),
                battle.getEntryCost(),
                dtos
        );
    }
}
