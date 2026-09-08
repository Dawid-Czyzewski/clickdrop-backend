package pl.clickdrop.economy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.clickdrop.economy.api.dto.ClickPowerResponse;
import pl.clickdrop.economy.application.ClickRewardService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/economy")
public class ClickPowerController {

    private final ClickRewardService clickRewardService;

    @GetMapping("/click-power")
    public ResponseEntity<ClickPowerResponse> getClickPower(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(
                new ClickPowerResponse(clickRewardService.calculateReward(userId))
        );
    }
}
