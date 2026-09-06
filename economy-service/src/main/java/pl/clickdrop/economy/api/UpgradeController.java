package pl.clickdrop.economy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.clickdrop.economy.api.dto.UpgradeResponse;
import pl.clickdrop.economy.application.UpgradeService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/economy/upgrades")
public class UpgradeController {

    private final UpgradeService upgradeService;

    @GetMapping
    public ResponseEntity<List<UpgradeResponse>> list() {
        return  ResponseEntity.ok(upgradeService.listActive());
    }
}
