package pl.clickdrop.economy.domain.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

    @Test
    void flywaySeed_loadsSixActiveItems() {
        List<Item> items = itemRepository.findByActiveTrueOrderByIdAsc();

        assertThat(items).hasSize(6);
        assertThat(items).extracting(Item::getCode)
                .containsExactly(
                        "GLOCK_WATER_ELEMENTAL",
                        "USP_GUARDIAN",
                        "AK_REDLINE",
                        "AWP_ELECTRIC_HIVE",
                        "M4A4_DESERT_STRIKE",
                        "AWP_ASIIMOV"
                );
    }

    @Test
    void findByCode_returnsSeededItemWithRarity() {
        Item ak = itemRepository.findByCode("AK_REDLINE").orElseThrow();

        assertThat(ak.getRarity()).isEqualTo(ItemRarity.RESTRICTED);
        assertThat(ak.isActive()).isTrue();
        assertThat(ak.getCreatedAt()).isNotNull();
    }

    @Test
    void findByCode_returnsCovertForAsiimov() {
        Item asiimov = itemRepository.findByCode("AWP_ASIIMOV").orElseThrow();

        assertThat(asiimov.getRarity()).isEqualTo(ItemRarity.COVERT);
    }

    @Test
    void rejectsDuplicateCode() {
        Item duplicate = new Item();
        duplicate.setCode("AK_REDLINE");
        duplicate.setRarity(ItemRarity.MIL_SPEC);

        assertThatThrownBy(() -> itemRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}