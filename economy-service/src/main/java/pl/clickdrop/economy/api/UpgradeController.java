package pl.clickdrop.economy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.clickdrop.economy.api.dto.BuyUpgradeResponse;
import pl.clickdrop.economy.api.dto.UpgradeResponse;
import pl.clickdrop.economy.application.UpgradePurchaseService;
import pl.clickdrop.economy.application.UpgradeService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/economy/upgrades")
public class UpgradeController {

    private final UpgradeService upgradeService;
    private final UpgradePurchaseService upgradePurchaseService;

    @GetMapping
    public ResponseEntity<List<UpgradeResponse>> list() {
        return  ResponseEntity.ok(upgradeService.listActive());
    }

    @PostMapping("/{upgradeId/buy}")
    public ResponseEntity<BuyUpgradeResponse> buy(
            Authentication authentication,
            @PathVariable Long upgradeId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(upgradePurchaseService.buy(userId, upgradeId));
    }
}
