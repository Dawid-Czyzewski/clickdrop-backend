package pl.clickdrop.economy.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.clickdrop.economy.application.exception.WalletAlreadyExistsException;
import pl.clickdrop.economy.application.exception.WalletNotFoundException;
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private static final BigDecimal CLICK_REWARD = BigDecimal.ONE;
    private final WalletRepository repository;

    @Transactional
    public Wallet createWalletForUser(Long userId) {
        if (repository.existsByUserId(userId)) {
            throw new WalletAlreadyExistsException(userId);
        }

        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);

        return repository.save(wallet);
    }

    @Transactional
    public BigDecimal getBalanceForUser(Long userId) {
        return repository.findByUserId(userId)
                .map(Wallet::getBalance)
                .orElseThrow(() -> new WalletNotFoundException(userId));
    }

    @Transactional
    public ClickResult clickForUser(Long userId) {
        Wallet wallet = repository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        wallet.setBalance(wallet.getBalance().add(CLICK_REWARD));
        Wallet saved = repository.save(wallet);

        return new ClickResult(saved.getBalance(), CLICK_REWARD);
    }
}