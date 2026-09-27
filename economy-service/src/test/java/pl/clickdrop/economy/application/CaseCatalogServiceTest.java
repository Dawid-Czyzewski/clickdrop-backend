package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.CaseDropResponse;
import pl.clickdrop.economy.application.exception.CaseNotFoundException;
import pl.clickdrop.economy.domain.cases.CaseDrop;
import pl.clickdrop.economy.domain.cases.CaseDropRepository;
import pl.clickdrop.economy.domain.cases.GameCase;
import pl.clickdrop.economy.domain.cases.GameCaseRepository;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemRarity;
import pl.clickdrop.economy.domain.item.ItemRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseCatalogServiceDropsTest {

    @Mock GameCaseRepository gameCaseRepository;
    @Mock CaseDropRepository caseDropRepository;
    @Mock ItemRepository itemRepository;

    @InjectMocks
    CaseCatalogService caseCatalogService;

    @Test
    void listDrops_returnsItemsWithWeight() {
        GameCase anvil = new GameCase();
        anvil.setId(1L);
        anvil.setActive(true);

        CaseDrop drop = new CaseDrop();
        drop.setCaseId(1L);
        drop.setItemId(3L);
        drop.setWeight(15);

        Item ak = new Item();
        ak.setId(3L);
        ak.setCode("AK_REDLINE");
        ak.setDisplayName("AK-47 | Redline");
        ak.setRarity(ItemRarity.RESTRICTED);
        ak.setActive(true);

        when(gameCaseRepository.findById(1L)).thenReturn(Optional.of(anvil));
        when(caseDropRepository.findByCaseId(1L)).thenReturn(List.of(drop));
        when(itemRepository.findAllById(anyCollection())).thenReturn(List.of(ak));

        List<CaseDropResponse> result = caseCatalogService.listDrops(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().itemId()).isEqualTo(3L);
        assertThat(result.getFirst().code()).isEqualTo("AK_REDLINE");
        assertThat(result.getFirst().displayName()).isEqualTo("AK-47 | Redline");
        assertThat(result.getFirst().rarity()).isEqualTo(ItemRarity.RESTRICTED);
        assertThat(result.getFirst().weight()).isEqualTo(15);
    }

    @Test
    void listDrops_throws_whenCaseMissing() {
        when(gameCaseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseCatalogService.listDrops(99L))
                .isInstanceOf(CaseNotFoundException.class);
    }

    @Test
    void listDrops_throws_whenCaseInactive() {
        GameCase inactive = new GameCase();
        inactive.setId(1L);
        inactive.setActive(false);
        when(gameCaseRepository.findById(1L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> caseCatalogService.listDrops(1L))
                .isInstanceOf(CaseNotFoundException.class);
    }
}