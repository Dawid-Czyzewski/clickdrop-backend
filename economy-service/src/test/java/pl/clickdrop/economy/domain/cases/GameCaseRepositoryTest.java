package pl.clickdrop.economy.domain.cases;

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
class GameCaseRepositoryTest {

    @Autowired
    GameCaseRepository gameCaseRepository;

    @Autowired
    CaseDropRepository caseDropRepository;

    @Test
    void flywaySeed_loadsTwelveActiveCsCases_orderedBySortOrder() {
        List<GameCase> cases = gameCaseRepository.findByActiveTrueOrderBySortOrderAsc();

        assertThat(cases).hasSize(12);
        assertThat(cases).extracting(GameCase::getCode)
                .contains(
                        "REVOLUTION_CASE",
                        "KILOWATT_CASE",
                        "DREAMS_NIGHTMARES_CASE"
                );
        assertThat(cases).allSatisfy(c -> {
            assertThat(c.getDisplayName()).isNotBlank();
            assertThat(c.getPrice()).isGreaterThan(BigDecimal.ZERO);
            assertThat(c.getMinLevel()).isNotNull().isGreaterThanOrEqualTo(1);
            assertThat(caseDropRepository.findByCaseId(c.getId())).isNotEmpty();
        });
    }

    @Test
    void legacyAnvil_isInactive_afterCsCatalogSeed() {
        GameCase anvil = gameCaseRepository.findByCode("ANVIL").orElseThrow();

        assertThat(anvil.isActive()).isFalse();
        assertThat(anvil.getPrice()).isEqualByComparingTo("50.00");
    }

    @Test
    void rejectsDuplicateCode() {
        GameCase duplicate = new GameCase();
        duplicate.setCode("ANVIL");
        duplicate.setDisplayName("Anvil");
        duplicate.setPrice(new BigDecimal("10.00"));
        duplicate.setSortOrder(99);

        assertThatThrownBy(() -> gameCaseRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
