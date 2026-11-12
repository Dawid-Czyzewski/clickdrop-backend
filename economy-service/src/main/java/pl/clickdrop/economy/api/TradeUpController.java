package pl.clickdrop.economy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.clickdrop.economy.api.dto.TradeUpRequest;
import pl.clickdrop.economy.api.dto.TradeUpResponse;
import pl.clickdrop.economy.application.TradeUpService;

@RestController
@RequestMapping("/api/economy")
@RequiredArgsConstructor
public class TradeUpController {

    private final TradeUpService tradeUpService;

    @PostMapping("/trade-up")
    public ResponseEntity<TradeUpResponse> tradeUp(
            Authentication authentication,
            @RequestBody TradeUpRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(tradeUpService.tradeUp(userId, request));
    }
}
