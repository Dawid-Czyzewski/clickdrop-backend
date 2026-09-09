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

    @Test
    void flywaySeed_loadsEightActiveCases_orderedBySortOrder() {
        List<GameCase> cases = gameCaseRepository.findByActiveTrueOrderBySortOrderASC();

        assertThat(cases).hasSize(8);
        assertThat(cases).extracting(GameCase::getCode)
                .containsExactly(
                        "ANVIL",
                        "TEMPER",
                        "FORGEFIRE",
                        "EMBER",
                        "NOVA",
                        "RIFT",
                        "PULSE",
                        "SHADOW"
                );
        assertThat(cases).extracting(GameCase::getSortOrder)
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8);
    }

    @Test
    void findByCode_returnsAnvilWithPrice() {
        GameCase anvil = gameCaseRepository.findByCode("ANVIL").orElseThrow();

        assertThat(anvil.getPrice()).isEqualByComparingTo("50.00");
        assertThat(anvil.getSortOrder()).isEqualTo(1);
        assertThat(anvil.isActive()).isTrue();
        assertThat(anvil.getCreatedAt()).isNotNull();
    }

    @Test
    void eachSeededCase_hasPositivePrice() {
        List<GameCase> cases = gameCaseRepository.findByActiveTrueOrderBySortOrderASC();

        assertThat(cases).allSatisfy(c ->
                assertThat(c.getPrice()).isGreaterThan(BigDecimal.ZERO)
        );
    }

    @Test
    void rejectsDuplicateCode() {
        GameCase duplicate = new GameCase();
        duplicate.setCode("ANVIL");
        duplicate.setPrice(new BigDecimal("10.00"));
        duplicate.setSortOrder(99);

        assertThatThrownBy(() -> gameCaseRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}