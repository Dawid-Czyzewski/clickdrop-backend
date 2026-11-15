package pl.clickdrop.inventory.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.clickdrop.inventory.api.dto.*;
import pl.clickdrop.inventory.application.InventoryService;
import pl.clickdrop.inventory.application.SkinUpgradeService;

import java.util.List;

@RestController
@RequestMapping("/api/economy/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final SkinUpgradeService skinUpgradeService;

    @GetMapping("/me")
    public ResponseEntity<List<InventoryItemResponse>> me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(inventoryService.listMine(userId));
    }

    @PostMapping("/sell-batch")
    public ResponseEntity<SellInventoryBatchResponse> sellBatch(
            Authentication authentication,
            @RequestBody SellInventoryBatchRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(inventoryService.sellBatch(userId, request));
    }

    @PostMapping("/{inventoryItemId}/sell")
    public ResponseEntity<SellInventoryItemResponse> sell(
            Authentication authentication,
            @PathVariable Long inventoryItemId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(inventoryService.sell(userId, inventoryItemId));
    }

    @GetMapping("/{inventoryItemId}/upgrade-preview")
    public ResponseEntity<SkinUpgradePreviewResponse> upgradePreview(
        Authentication authentication,
        @PathVariable Long inventoryItemId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(skinUpgradeService.preview(userId, inventoryItemId));
    }

    @PostMapping("/{inventoryItemId}/upgrade")
    public ResponseEntity<SkinUpgradeResponse> upgrade(
        Authentication authentication,
        @PathVariable Long inventoryItemId,
        @RequestBody SkinUpgradeRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(skinUpgradeService.upgrade(userId, inventoryItemId, request));
    }
}
