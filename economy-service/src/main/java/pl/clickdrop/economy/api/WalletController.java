package pl.clickdrop.economy.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.clickdrop.economy.api.dto.CreateWalletRequest;
import pl.clickdrop.economy.api.dto.WalletResponse;
import pl.clickdrop.economy.application.WalletService;
import pl.clickdrop.economy.domain.wallet.Wallet;

@RestController
@RequestMapping("/api/economy/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> create(@Valid @RequestBody CreateWalletRequest request) {
        Wallet wallet = walletService.createWalletForUser(request.userId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new WalletResponse(wallet.getId(), wallet.getUserId(), wallet.getBalance()));
    }
}