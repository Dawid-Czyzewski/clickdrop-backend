package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.ItemResponse;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemRarity;
import pl.clickdrop.economy.domain.item.ItemRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    ItemRepository itemRepository;

    @InjectMocks
    ItemService itemService;

    @Test
    void listActive_mapsEntitiesToResponse_withoutName() {
        Item ak = new Item();
        ak.setId(3L);
        ak.setCode("AK_REDLINE");
        ak.setRarity(ItemRarity.RESTRICTED);
        ak.setActive(true);

        when(itemRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(ak));

        List<ItemResponse> result = itemService.listActive();

        assertThat(result).hasSize(1);
        ItemResponse dto = result.getFirst();
        assertThat(dto.id()).isEqualTo(3L);
        assertThat(dto.code()).isEqualTo("AK_REDLINE");
        assertThat(dto.rarity()).isEqualTo(ItemRarity.RESTRICTED);

        verify(itemRepository).findByActiveTrueOrderByIdAsc();
    }

    @Test
    void listActive_returnsEmptyList_whenNoneActive() {
        when(itemRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of());

        assertThat(itemService.listActive()).isEmpty();
    }
}