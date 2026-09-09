package pl.clickdrop.economy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.clickdrop.economy.api.dto.ItemResponse;
import pl.clickdrop.economy.application.ItemService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/economy/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<List<ItemResponse>> list() {
        return ResponseEntity.ok(itemService.listActive());
    }
}
