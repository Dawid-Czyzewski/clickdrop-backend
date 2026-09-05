package pl.clickdrop.economy;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.clickdrop.economy.api.dto.ClickResponse;
import pl.clickdrop.economy.application.ClickResult;
import pl.clickdrop.economy.application.WalletService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/economy")
public class ClickController {

    private final WalletService walletService;

    @PostMapping("/click")
    public ResponseEntity<ClickResponse> click(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ClickResult result = walletService.clickForUser(userId);

        return ResponseEntity.ok(new ClickResponse(result.balance(), result.earned()));
    }
}
