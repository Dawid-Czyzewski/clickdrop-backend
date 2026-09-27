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
    void flywaySeed_loadsLegacyAndCsCatalogItems() {
        List<Item> items = itemRepository.findByActiveTrueOrderByIdAsc();

        assertThat(items.size()).isGreaterThanOrEqualTo(100);
        assertThat(items).extracting(Item::getCode)
                .contains(
                        "GLOCK_WATER_ELEMENTAL",
                        "USP_GUARDIAN",
                        "AK_REDLINE",
                        "AWP_ASIIMOV"
                );
        assertThat(items).allSatisfy(item ->
                assertThat(item.getDisplayName()).isNotBlank()
        );
    }

    @Test
    void findByCode_returnsSeededItemWithRarityAndDisplayName() {
        Item ak = itemRepository.findByCode("AK_REDLINE").orElseThrow();

        assertThat(ak.getRarity()).isEqualTo(ItemRarity.RESTRICTED);
        assertThat(ak.getDisplayName()).isEqualTo("AK-47 | Redline");
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
        duplicate.setDisplayName("dup");
        duplicate.setRarity(ItemRarity.MIL_SPEC);

        assertThatThrownBy(() -> itemRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
