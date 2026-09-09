package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.CaseResponse;
import pl.clickdrop.economy.domain.cases.GameCase;
import pl.clickdrop.economy.domain.cases.GameCaseRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseCatalogServiceTest {

    @Mock
    GameCaseRepository gameCaseRepository;

    @InjectMocks
    CaseCatalogService caseCatalogService;

    @Test
    void listActive_mapsEntitiesToResponse_withoutName() {
        GameCase anvil = new GameCase();
        anvil.setId(1L);
        anvil.setCode("ANVIL");
        anvil.setPrice(new BigDecimal("50.00"));
        anvil.setSortOrder(1);
        anvil.setActive(true);

        when(gameCaseRepository.findByActiveTrueOrderBySortOrderASC())
                .thenReturn(List.of(anvil));

        List<CaseResponse> result = caseCatalogService.listActive();

        assertThat(result).hasSize(1);
        CaseResponse dto = result.getFirst();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.code()).isEqualTo("ANVIL");
        assertThat(dto.price()).isEqualByComparingTo("50.00");
        assertThat(dto.sortOrder()).isEqualTo(1);

        verify(gameCaseRepository).findByActiveTrueOrderBySortOrderASC();
    }

    @Test
    void listActive_returnsEmptyList_whenNoneActive() {
        when(gameCaseRepository.findByActiveTrueOrderBySortOrderASC())
                .thenReturn(List.of());

        assertThat(caseCatalogService.listActive()).isEmpty();
    }
}