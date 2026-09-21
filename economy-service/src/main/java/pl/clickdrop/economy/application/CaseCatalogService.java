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

import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        return drops.stream()
                .map(drop -> {
                    Item item = itemsById.get(drop.getItemId());

                    if (item == null || !item.isActive()) {
                        return null;
                    }

                    return new CaseDropResponse(
                            item.getId(),
                            item.getCode(),
                            item.getRarity(),
                            drop.getWeight()
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private CaseResponse toResponse(GameCase gameCase) {
        return new CaseResponse(
                gameCase.getId(),
                gameCase.getCode(),
                gameCase.getPrice(),
                gameCase.getSortOrder()
        );
    }
}
