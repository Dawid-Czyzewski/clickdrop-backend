package pl.clickdrop.economy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.clickdrop.economy.api.dto.InventoryItemResponse;
import pl.clickdrop.economy.api.dto.SellInventoryItemResponse;
import pl.clickdrop.economy.application.InventoryService;

import java.util.List;

@RestController
@RequestMapping("/api/economy/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/me")
    public ResponseEntity<List<InventoryItemResponse>> me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(inventoryService.listMine(userId));
    }

    @PostMapping("/{inventoryItemId}/sell")
    public ResponseEntity<SellInventoryItemResponse> sell(
            Authentication authentication,
            @PathVariable Long inventoryItemId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(inventoryService.sell(userId, inventoryItemId));
    }
}
