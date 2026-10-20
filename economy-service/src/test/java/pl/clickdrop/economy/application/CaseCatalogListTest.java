package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.CaseResponse;
import pl.clickdrop.economy.domain.cases.CaseDropRepository;
import pl.clickdrop.economy.domain.cases.GameCase;
import pl.clickdrop.economy.domain.cases.GameCaseRepository;
import pl.clickdrop.economy.domain.item.ItemRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseCatalogListTest {

    @Mock GameCaseRepository gameCaseRepository;
    @Mock CaseDropRepository caseDropRepository;
    @Mock ItemRepository itemRepository;

    @InjectMocks
    CaseCatalogService caseCatalogService;

    @Test
    void listActive_includesMinLevel() {
        GameCase c = new GameCase();
        c.setId(1L);
        c.setCode("CS20_CASE");
        c.setDisplayName("CS20 Case");
        c.setPrice(new BigDecimal("65.00"));
        c.setSortOrder(11);
        c.setMinLevel(1);
        c.setActive(true);

        when(gameCaseRepository.findByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of(c));

        List<CaseResponse> result = caseCatalogService.listActive();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().minLevel()).isEqualTo(1);
        assertThat(result.getFirst().code()).isEqualTo("CS20_CASE");
    }
}
