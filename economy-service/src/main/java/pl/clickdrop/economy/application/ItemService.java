package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.economy.api.dto.ItemResponse;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public List<ItemResponse> listActive() {
        return itemRepository.findByActiveTrueOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ItemResponse toResponse(Item item) {
        return new ItemResponse(item.getId(), item.getCode(), item.getRarity());
    }
}
