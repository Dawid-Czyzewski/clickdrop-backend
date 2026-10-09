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
    void listDrops_returnsWeightAndChancePercent() {
        GameCase anvil = activeCase(1L);
        CaseDrop a = drop(1L, 3L, 15);
        CaseDrop b = drop(1L, 4L, 45);
        Item ak = item(3L, "AK_REDLINE", "AK-47 | Redline", ItemRarity.RESTRICTED);
        Item awp = item(4L, "AWP_ASIIMOV", "AWP | Asiimov", ItemRarity.COVERT);

        when(gameCaseRepository.findById(1L)).thenReturn(Optional.of(anvil));
        when(caseDropRepository.findByCaseId(1L)).thenReturn(List.of(a, b));
        when(itemRepository.findAllById(anyCollection())).thenReturn(List.of(ak, awp));

        List<CaseDropResponse> result = caseCatalogService.listDrops(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).weight()).isEqualTo(15);
        assertThat(result.get(0).chancePercent()).isEqualByComparingTo("25.00");
        assertThat(result.get(1).weight()).isEqualTo(45);
        assertThat(result.get(1).chancePercent()).isEqualByComparingTo("75.00");
    }

    @Test
    void listDrops_singleItem_is100Percent() {
        GameCase anvil = activeCase(1L);
        CaseDrop drop = drop(1L, 3L, 15);
        Item ak = item(3L, "AK_REDLINE", "AK-47 | Redline", ItemRarity.RESTRICTED);

        when(gameCaseRepository.findById(1L)).thenReturn(Optional.of(anvil));
        when(caseDropRepository.findByCaseId(1L)).thenReturn(List.of(drop));
        when(itemRepository.findAllById(anyCollection())).thenReturn(List.of(ak));

        List<CaseDropResponse> result = caseCatalogService.listDrops(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().weight()).isEqualTo(15);
        assertThat(result.getFirst().chancePercent()).isEqualByComparingTo("100.00");
    }

    @Test
    void listDrops_throws_whenCaseMissing() {
        when(gameCaseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseCatalogService.listDrops(99L))
                .isInstanceOf(CaseNotFoundException.class);
    }

    @Test
    void listDrops_throws_whenCaseInactive() {
        GameCase inactive = activeCase(1L);
        inactive.setActive(false);
        when(gameCaseRepository.findById(1L)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> caseCatalogService.listDrops(1L))
                .isInstanceOf(CaseNotFoundException.class);
    }

    private static GameCase activeCase(Long id) {
        GameCase c = new GameCase();
        c.setId(id);
        c.setActive(true);
        return c;
    }

    private static CaseDrop drop(Long caseId, Long itemId, int weight) {
        CaseDrop d = new CaseDrop();
        d.setCaseId(caseId);
        d.setItemId(itemId);
        d.setWeight(weight);
        return d;
    }

    private static Item item(Long id, String code, String name, ItemRarity rarity) {
        Item i = new Item();
        i.setId(id);
        i.setCode(code);
        i.setDisplayName(name);
        i.setRarity(rarity);
        i.setActive(true);
        return i;
    }
}