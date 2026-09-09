package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.clickdrop.economy.api.dto.CaseResponse;
import pl.clickdrop.economy.domain.cases.GameCase;
import pl.clickdrop.economy.domain.cases.GameCaseRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseCatalogService {

    private final GameCaseRepository gameCaseRepository;

    public List<CaseResponse> listActive() {
        return gameCaseRepository.findByActiveTrueOrderBySortOrderASC()
                .stream()
                .map(this::toResponse)
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
