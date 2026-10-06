package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.economy.api.dto.IdleCollectResponse;
import pl.clickdrop.economy.api.dto.IdleStatusResponse;
import pl.clickdrop.economy.application.exception.WalletNotFoundException;
import pl.clickdrop.economy.domain.upgrade.Upgrade;
import pl.clickdrop.economy.domain.upgrade.UpgradeRepository;
import pl.clickdrop.economy.domain.upgrade.UserUpgrade;
import pl.clickdrop.economy.domain.upgrade.UserUpgradeRepository;
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IdleIncomeService {

    private static final long MAX_IDLE_SECONDS = 8L * 3600;

    private final WalletRepository walletRepository;
    private final UserUpgradeRepository userUpgradeRepository;
    private final UpgradeRepository upgradeRepository;

    @Transactional(readOnly = true)
    public IdleStatusResponse status(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        Instant now = Instant.now();
        BigDecimal rate = currentIncomePerSecond(userId);
        BigDecimal pending = pendingAmount(wallet.getLastIdleCollectedAt(), now, rate);

        return new IdleStatusResponse(pending, rate, wallet.getLastIdleCollectedAt());
    }

    @Transactional
    public IdleCollectResponse collect(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));

        Instant now = Instant.now();
        BigDecimal rate = currentIncomePerSecond(userId);
        BigDecimal earned = pendingAmount(wallet.getLastIdleCollectedAt(), now, rate);

        if (earned.signum() > 0) {
            wallet.setBalance(wallet.getBalance().add(earned));
        }
        wallet.setLastIdleCollectedAt(now);
        walletRepository.save(wallet);

        return new IdleCollectResponse(earned, wallet.getBalance(), rate);
    }

    @Transactional(readOnly = true)
    public BigDecimal currentIncomePerSecond(long userId) {
        List<UserUpgrade> owned = userUpgradeRepository.findByUserId(userId);

        if (owned.isEmpty()) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }

        Map<Long, Upgrade> catalogById = upgradeRepository.findAllById(
                owned.stream().map(UserUpgrade::getUpgradeId).toList()
        ).stream().collect(Collectors.toMap(Upgrade::getId, Function.identity()));

        BigDecimal rate = BigDecimal.ZERO;

        for (UserUpgrade uu : owned) {
            if (uu.getLevel() == null || uu.getLevel() <= 0) {
                continue;
            }

            Upgrade catalog = catalogById.get(uu.getUpgradeId());

            if (catalog == null || !catalog.isActive()) {
                continue;
            }

            rate = rate.add(
                    catalog.getIncomePerSecond().multiply(BigDecimal.valueOf(uu.getLevel()))
            );
        }

        return rate.setScale(4, RoundingMode.HALF_UP);
    }

    private static BigDecimal pendingAmount(Instant lastCollectedAt, Instant now, BigDecimal rate) {
        long seconds = Duration.between(lastCollectedAt, now).getSeconds();
        if (seconds < 0) {
            seconds = 0;
        }
        seconds = Math.min(seconds, MAX_IDLE_SECONDS);

        return rate
                .multiply(BigDecimal.valueOf(seconds))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
