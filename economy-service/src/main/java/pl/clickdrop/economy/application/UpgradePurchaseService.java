package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.economy.api.dto.BuyUpgradeResponse;
import pl.clickdrop.economy.application.exception.InsufficientFundsException;
import pl.clickdrop.economy.application.exception.UpgradeNotFoundException;
import pl.clickdrop.economy.application.exception.WalletNotFoundException;
import pl.clickdrop.economy.domain.upgrade.Upgrade;
import pl.clickdrop.economy.domain.upgrade.UpgradeRepository;
import pl.clickdrop.economy.domain.upgrade.UserUpgrade;
import pl.clickdrop.economy.domain.upgrade.UserUpgradeRepository;
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UpgradePurchaseService {

    private final UpgradeRepository upgradeRepository;
    private final UserUpgradeRepository userUpgradeRepository;
    private final WalletRepository walletRepository;

    @Transactional
    public BuyUpgradeResponse buy(Long userId, Long upgradeId) {
        Upgrade catalog = upgradeRepository.findById(upgradeId)
                .filter(Upgrade::isActive)
                .orElseThrow(() -> new UpgradeNotFoundException(upgradeId));

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        UserUpgrade userUpgrade = userUpgradeRepository
                .findByUserIdAndUpgradeId(userId, upgradeId)
                .orElseGet(() -> newUserUpgrade(userId, upgradeId));

        int currentLevel = userUpgrade.getLevel() == null ? 0 : userUpgrade.getLevel();
        BigDecimal cost = UpgradePricing.nextCost(catalog.getPrice(), currentLevel);

        if (wallet.getBalance().compareTo(cost) < 0) {
            throw new InsufficientFundsException();
        }

        wallet.setBalance(wallet.getBalance().subtract(cost));
        userUpgrade.setLevel(currentLevel + 1);


        walletRepository.save(wallet);
        userUpgradeRepository.save(userUpgrade);

        return new BuyUpgradeResponse(
            catalog.getId(),
            catalog.getCode(),
            userUpgrade.getLevel(),
            wallet.getBalance(),
            cost
        );
    }

    private static UserUpgrade newUserUpgrade(Long userId, Long upgradeId) {
        UserUpgrade uu = new UserUpgrade();
        uu.setUserId(userId);
        uu.setUpgradeId(upgradeId);
        uu.setLevel(0);
        return uu;
    }
}
