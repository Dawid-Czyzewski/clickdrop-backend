package pl.clickdrop.economy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.clickdrop.economy.api.dto.DailyClaimResponse;
import pl.clickdrop.economy.api.dto.DailyStatusResponse;
import pl.clickdrop.economy.application.DailyRewardService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/economy/daily")
public class DailyRewardController {

    private final DailyRewardService dailyRewardService;

    @GetMapping("/status")
    public ResponseEntity<DailyStatusResponse> status(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(dailyRewardService.status(userId));
    }

    @PostMapping("/claim")
    public ResponseEntity<DailyClaimResponse> claim(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(dailyRewardService.claim(userId));
    }
}
