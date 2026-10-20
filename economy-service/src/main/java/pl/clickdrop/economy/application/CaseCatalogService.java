package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.economy.api.dto.CaseDropResponse;
import pl.clickdrop.economy.api.dto.CaseResponse;
import pl.clickdrop.economy.application.exception.CaseNotFoundException;
import pl.clickdrop.economy.domain.cases.CaseDrop;
import pl.clickdrop.economy.domain.cases.CaseDropRepository;
import pl.clickdrop.economy.domain.cases.GameCase;
import pl.clickdrop.economy.domain.cases.GameCaseRepository;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaseCatalogService {

    private final GameCaseRepository gameCaseRepository;
    private final CaseDropRepository caseDropRepository;
    private final ItemRepository itemRepository;

    public List<CaseResponse> listActive() {
        return gameCaseRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CaseDropResponse> listDrops(Long caseId) {
        GameCase gameCase = gameCaseRepository.findById(caseId)
                .filter(GameCase::isActive)
                .orElseThrow(() -> new CaseNotFoundException(caseId));

        List<CaseDrop> drops = caseDropRepository.findByCaseId(gameCase.getId());
        if (drops.isEmpty()) {
            return List.of();
        }

        Map<Long, Item> itemsById = itemRepository.findAllById(
                drops.stream().map(CaseDrop::getItemId).toList()
        ).stream().collect(Collectors.toMap(Item::getId, Function.identity()));

        record Resolved(CaseDrop drop, Item item) {}

        List<Resolved> resolved = new ArrayList<>();
        for (CaseDrop drop : drops) {
            Item item = itemsById.get(drop.getItemId());
            if (item != null && item.isActive()) {
                resolved.add(new Resolved(drop, item));
            }
        }

        if (resolved.isEmpty()) {
            return List.of();
        }

        int totalWeight = resolved.stream()
                .mapToInt(r -> r.drop().getWeight())
                .sum();

        return resolved.stream()
                .map(r -> new CaseDropResponse(
                        r.item().getId(),
                        r.item().getCode(),
                        r.item().getDisplayName(),
                        r.item().getRarity(),
                        r.drop().getWeight(),
                        DropChance.percent(r.drop().getWeight(), totalWeight)
                ))
                .toList();
    }

    private CaseResponse toResponse(GameCase gameCase) {
        return new CaseResponse(
                gameCase.getId(),
                gameCase.getCode(),
                gameCase.getDisplayName(),
                gameCase.getPrice(),
                gameCase.getSortOrder(),
                gameCase.getMinLevel() == null ? 1 : gameCase.getMinLevel()
        );
    }
}