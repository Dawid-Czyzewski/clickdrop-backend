package pl.clickdrop.economy.domain.upgrade;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UpgradeRepositoryTest {

    @Autowired
    UpgradeRepository upgradeRepository;

    @Test
    void flywaySeed_loadsFiveActiveUpgrades_orderedByTier() {
        List<Upgrade> upgrades = upgradeRepository.findByActiveTrueOrderByTierAsc();

        assertThat(upgrades).hasSize(5);
        assertThat(upgrades).extracting(Upgrade::getCode)
                .containsExactly(
                        "BETTER_MOUSE",
                        "GAMING_MOUSE",
                        "MECHANICAL_KEYBOARD",
                        "GAMING_SETUP",
                        "AI_AUTO_CLICKER"
                );
        assertThat(upgrades).extracting(Upgrade::getTier)
                .containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void eachSeededUpgrade_hasPriceAndClickPower() {
        List<Upgrade> upgrades = upgradeRepository.findByActiveTrueOrderByTierAsc();

        assertThat(upgrades).allSatisfy(u -> {
            assertThat(u.getPrice()).isGreaterThan(BigDecimal.ZERO);
            assertThat(u.getClickPower()).isGreaterThan(BigDecimal.ZERO);
            assertThat(u.getCode()).isNotBlank();
            assertThat(u.isActive()).isTrue();
        });
    }

    @Test
    void findByCode_returnsSeededUpgrade() {
        Upgrade found = upgradeRepository.findByCode("BETTER_MOUSE").orElseThrow();

        assertThat(found.getTier()).isEqualTo(1);
        assertThat(found.getPrice()).isEqualByComparingTo("50.00");
        assertThat(found.getClickPower()).isEqualByComparingTo("1.00");
    }

    @Test
    void findByActiveTrue_excludesInactive() {
        Upgrade upgrade = upgradeRepository.findByCode("AI_AUTO_CLICKER").orElseThrow();
        upgrade.setActive(false);
        upgradeRepository.saveAndFlush(upgrade);

        List<Upgrade> active = upgradeRepository.findByActiveTrueOrderByTierAsc();

        assertThat(active).extracting(Upgrade::getCode)
                .doesNotContain("AI_AUTO_CLICKER")
                .hasSize(4);

        upgrade.setActive(true);
        upgradeRepository.saveAndFlush(upgrade);
    }

    @Test
    void rejectsDuplicateCode() {
        Upgrade duplicate = new Upgrade();
        duplicate.setCode("BETTER_MOUSE");
        duplicate.setName("Dup");
        duplicate.setDescription("Dup");
        duplicate.setTier(9);
        duplicate.setPrice(BigDecimal.TEN);
        duplicate.setClickPower(BigDecimal.ONE);

        assertThatThrownBy(() -> upgradeRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}