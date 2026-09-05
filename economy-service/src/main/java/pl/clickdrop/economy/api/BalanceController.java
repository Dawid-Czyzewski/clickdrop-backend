package pl.clickdrop.economy.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.clickdrop.economy.api.dto.BalanceResponse;
import pl.clickdrop.economy.application.WalletService;

@RestController
@RequestMapping("/api/economy")
@RequiredArgsConstructor
public class BalanceController {

    private final WalletService walletService;

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(new BalanceResponse(walletService.getBalanceForUser(userId)));
    }
}