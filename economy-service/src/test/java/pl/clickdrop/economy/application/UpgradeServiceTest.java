package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.MyUpgradeResponse;
import pl.clickdrop.economy.api.dto.UpgradeResponse;
import pl.clickdrop.economy.domain.upgrade.Upgrade;
import pl.clickdrop.economy.domain.upgrade.UpgradeRepository;
import pl.clickdrop.economy.domain.upgrade.UserUpgrade;
import pl.clickdrop.economy.domain.upgrade.UserUpgradeRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpgradeServiceTest {

    @Mock
    UpgradeRepository repository;

    @InjectMocks
    UpgradeService upgradeService;

    @Mock
    UserUpgradeRepository userUpgradeRepository;

    @Test
    void listActive_mapsEntitiesToResponse_withCodeForI18n() {
        Upgrade mouse = new Upgrade();
        mouse.setId(1L);
        mouse.setCode("BETTER_MOUSE");
        mouse.setName("Better Mouse");
        mouse.setDescription("EN seed text");
        mouse.setTier(1);
        mouse.setPrice(new BigDecimal("50.00"));
        mouse.setClickPower(new BigDecimal("1.00"));
        mouse.setActive(true);

        when(repository.findByActiveTrueOrderByTierAsc()).thenReturn(List.of(mouse));

        List<UpgradeResponse> result = upgradeService.listActive();

        assertThat(result).hasSize(1);
        UpgradeResponse dto = result.getFirst();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.code()).isEqualTo("BETTER_MOUSE");
        assertThat(dto.tier()).isEqualTo(1);
        assertThat(dto.price()).isEqualByComparingTo("50.00");
        assertThat(dto.clickPower()).isEqualByComparingTo("1.00");

        verify(repository).findByActiveTrueOrderByTierAsc();
    }

    @Test
    void listActive_returnsEmptyList_whenNoneActive() {
        when(repository.findByActiveTrueOrderByTierAsc()).thenReturn(List.of());

        assertThat(upgradeService.listActive()).isEmpty();
    }

    @Test
    void listMine_mergesCatalogWithLevels_defaultZero() {
        Upgrade mouse = upgrade(1L, "BETTER_MOUSE", 1, "50", "1");
        Upgrade gaming = upgrade(2L, "GAMING_MOUSE", 2, "200", "2");
        UserUpgrade owned = new UserUpgrade();
        owned.setUserId(15L);
        owned.setUpgradeId(1L);
        owned.setLevel(3);
        when(repository.findByActiveTrueOrderByTierAsc()).thenReturn(List.of(mouse, gaming));
        when(userUpgradeRepository.findByUserId(15L)).thenReturn(List.of(owned));
        List<MyUpgradeResponse> result = upgradeService.listMine(15L);
        assertThat(result).hasSize(2);
        MyUpgradeResponse first = result.getFirst();
        assertThat(first.code()).isEqualTo("BETTER_MOUSE");
        assertThat(first.level()).isEqualTo(3);
        assertThat(first.nextCost()).isEqualByComparingTo("50");
        assertThat(first.price()).isEqualByComparingTo("50");
        assertThat(first.clickPower()).isEqualByComparingTo("1");
        MyUpgradeResponse second = result.get(1);
        assertThat(second.code()).isEqualTo("GAMING_MOUSE");
        assertThat(second.level()).isZero();
        assertThat(second.nextCost()).isEqualByComparingTo("200");
    }
    @Test
    void listMine_allZero_whenUserHasNoRows() {
        Upgrade mouse = upgrade(1L, "BETTER_MOUSE", 1, "50", "1");
        when(repository.findByActiveTrueOrderByTierAsc()).thenReturn(List.of(mouse));
        when(userUpgradeRepository.findByUserId(15L)).thenReturn(List.of());
        List<MyUpgradeResponse> result = upgradeService.listMine(15L);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().level()).isZero();
    }
    private static Upgrade upgrade(
            Long id, String code, int tier, String price, String power
    ) {
        Upgrade u = new Upgrade();
        u.setId(id);
        u.setCode(code);
        u.setTier(tier);
        u.setPrice(new BigDecimal(price));
        u.setClickPower(new BigDecimal(power));
        u.setActive(true);
        return u;
    }
}