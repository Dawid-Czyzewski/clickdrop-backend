package pl.clickdrop.economy.domain.cases;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CaseDropRepositoryTest {

    @Autowired
    CaseDropRepository caseDropRepository;

    @Autowired
    GameCaseRepository gameCaseRepository;

    @Autowired
    ItemRepository itemRepository;

    @Test
    void anvil_hasFourWeightedDrops() {
        Long anvilId = gameCaseRepository.findByCode("ANVIL").orElseThrow().getId();

        List<CaseDrop> drops = caseDropRepository.findByCaseId(anvilId);

        assertThat(drops).hasSize(4);
        assertThat(drops).allMatch(d -> d.getWeight() > 0);
        assertThat(drops).extracting(CaseDrop::getWeight)
                .containsExactlyInAnyOrder(50, 30, 15, 5);
    }

    @Test
    void temper_hasFourWeightedDrops() {
        Long temperId = gameCaseRepository.findByCode("TEMPER").orElseThrow().getId();

        List<CaseDrop> drops = caseDropRepository.findByCaseId(temperId);

        assertThat(drops).hasSize(4);
        assertThat(drops).extracting(CaseDrop::getWeight)
                .containsExactlyInAnyOrder(40, 35, 20, 5);
    }

    @Test
    void anvilDrops_referenceExistingItems() {
        Long anvilId = gameCaseRepository.findByCode("ANVIL").orElseThrow().getId();
        List<CaseDrop> drops = caseDropRepository.findByCaseId(anvilId);

        assertThat(drops).allSatisfy(drop ->
                assertThat(itemRepository.findById(drop.getItemId())).isPresent()
        );
    }

    @Test
    void rejectsDuplicateCaseAndItem() {
        Long anvilId = gameCaseRepository.findByCode("ANVIL").orElseThrow().getId();
        Item glock = itemRepository.findByCode("GLOCK_WATER_ELEMENTAL").orElseThrow();

        CaseDrop duplicate = new CaseDrop();
        duplicate.setCaseId(anvilId);
        duplicate.setItemId(glock.getId());
        duplicate.setWeight(10);

        assertThatThrownBy(() -> caseDropRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}