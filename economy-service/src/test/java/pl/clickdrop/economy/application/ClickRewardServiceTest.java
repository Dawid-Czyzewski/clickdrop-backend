package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.domain.upgrade.Upgrade;
import pl.clickdrop.economy.domain.upgrade.UpgradeRepository;
import pl.clickdrop.economy.domain.upgrade.UserUpgrade;
import pl.clickdrop.economy.domain.upgrade.UserUpgradeRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClickRewardServiceTest {

    @Mock
    UserUpgradeRepository userUpgradeRepository;

    @Mock
    UpgradeRepository upgradeRepository;

    @InjectMocks
    ClickRewardService clickRewardService;

    @Test
    void returnsBaseReward_whenUserHasNoUpgrades() {
        when(userUpgradeRepository.findByUserId(15L)).thenReturn(List.of());

        assertThat(clickRewardService.calculateReward(15L))
                .isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void betterMouseLevel1_givesTwo() {
        UserUpgrade uu = userUpgrade(15L, 1L, 1);
        Upgrade mouse = catalog(1L, "1.00");

        when(userUpgradeRepository.findByUserId(15L)).thenReturn(List.of(uu));
        when(upgradeRepository.findAllById(anyCollection())).thenReturn(List.of(mouse));

        assertThat(clickRewardService.calculateReward(15L))
                .isEqualByComparingTo("2");
    }

    @Test
    void betterMouseLevel3_givesFour() {
        UserUpgrade uu = userUpgrade(15L, 1L, 3);
        Upgrade mouse = catalog(1L, "1.00");

        when(userUpgradeRepository.findByUserId(15L)).thenReturn(List.of(uu));
        when(upgradeRepository.findAllById(anyCollection())).thenReturn(List.of(mouse));

        assertThat(clickRewardService.calculateReward(15L))
                .isEqualByComparingTo("4");
    }

    @Test
    void sumsMultipleUpgrades() {
        UserUpgrade mouse = userUpgrade(15L, 1L, 1);
        UserUpgrade gaming = userUpgrade(15L, 2L, 1);
        Upgrade c1 = catalog(1L, "1.00");
        Upgrade c2 = catalog(2L, "2.00");

        when(userUpgradeRepository.findByUserId(15L)).thenReturn(List.of(mouse, gaming));
        when(upgradeRepository.findAllById(anyCollection())).thenReturn(List.of(c1, c2));

        assertThat(clickRewardService.calculateReward(15L))
                .isEqualByComparingTo("4");
    }

    @Test
    void ignoresLevelZero() {
        UserUpgrade uu = userUpgrade(15L, 1L, 0);
        Upgrade mouse = catalog(1L, "1.00");

        when(userUpgradeRepository.findByUserId(15L)).thenReturn(List.of(uu));
        when(upgradeRepository.findAllById(anyCollection())).thenReturn(List.of(mouse));

        assertThat(clickRewardService.calculateReward(15L))
                .isEqualByComparingTo(BigDecimal.ONE);
    }

    private static UserUpgrade userUpgrade(Long userId, Long upgradeId, int level) {
        UserUpgrade uu = new UserUpgrade();
        uu.setUserId(userId);
        uu.setUpgradeId(upgradeId);
        uu.setLevel(level);
        return uu;
    }

    private static Upgrade catalog(Long id, String clickPower) {
        Upgrade u = new Upgrade();
        u.setId(id);
        u.setClickPower(new BigDecimal(clickPower));
        u.setActive(true);
        return u;
    }
}