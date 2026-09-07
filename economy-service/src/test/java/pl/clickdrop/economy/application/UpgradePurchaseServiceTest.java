package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.BuyUpgradeResponse;
import pl.clickdrop.economy.application.exception.InsufficientFundsException;
import pl.clickdrop.economy.application.exception.UpgradeNotFoundException;
import pl.clickdrop.economy.domain.upgrade.Upgrade;
import pl.clickdrop.economy.domain.upgrade.UpgradeRepository;
import pl.clickdrop.economy.domain.upgrade.UserUpgrade;
import pl.clickdrop.economy.domain.upgrade.UserUpgradeRepository;
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpgradePurchaseServiceTest {

    @Mock UpgradeRepository upgradeRepository;
    @Mock UserUpgradeRepository userUpgradeRepository;
    @Mock WalletRepository walletRepository;

    @InjectMocks
    UpgradePurchaseService purchaseService;

    @Test
    void buy_firstTime_createsLevel1_andChargesPrice() {
        Upgrade catalog = catalog(1L, "BETTER_MOUSE", "50");
        Wallet wallet = wallet(15L, "100");

        when(upgradeRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(userUpgradeRepository.findByUserIdAndUpgradeId(15L, 1L)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));
        when(userUpgradeRepository.save(any(UserUpgrade.class))).thenAnswer(i -> i.getArgument(0));

        BuyUpgradeResponse res = purchaseService.buy(15L, 1L);

        assertThat(res.level()).isEqualTo(1);
        assertThat(res.cost()).isEqualByComparingTo("50");
        assertThat(res.balance()).isEqualByComparingTo("50");
        assertThat(res.code()).isEqualTo("BETTER_MOUSE");
        verify(userUpgradeRepository).save(argThat(uu -> uu.getLevel() == 1));
    }

    @Test
    void buy_existing_incrementsLevel() {
        Upgrade catalog = catalog(1L, "BETTER_MOUSE", "50");
        Wallet wallet = wallet(15L, "100");
        UserUpgrade owned = userUpgrade(15L, 1L, 2);

        when(upgradeRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(userUpgradeRepository.findByUserIdAndUpgradeId(15L, 1L)).thenReturn(Optional.of(owned));
        when(walletRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userUpgradeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BuyUpgradeResponse res = purchaseService.buy(15L, 1L);

        assertThat(res.level()).isEqualTo(3);
        assertThat(res.balance()).isEqualByComparingTo("50");
    }

    @Test
    void buy_throws_whenInsufficientFunds_andDoesNotSave() {
        Upgrade catalog = catalog(1L, "BETTER_MOUSE", "50");
        Wallet wallet = wallet(15L, "10");

        when(upgradeRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(userUpgradeRepository.findByUserIdAndUpgradeId(15L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.buy(15L, 1L))
                .isInstanceOf(InsufficientFundsException.class);

        verify(walletRepository, never()).save(any());
        verify(userUpgradeRepository, never()).save(any());
        assertThat(wallet.getBalance()).isEqualByComparingTo("10");
    }

    @Test
    void buy_throws_whenUpgradeMissing() {
        when(upgradeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.buy(15L, 99L))
                .isInstanceOf(UpgradeNotFoundException.class);

        verifyNoInteractions(walletRepository, userUpgradeRepository);
    }

    private static Upgrade catalog(Long id, String code, String price) {
        Upgrade u = new Upgrade();
        u.setId(id);
        u.setCode(code);
        u.setPrice(new BigDecimal(price));
        u.setActive(true);
        return u;
    }

    private static Wallet wallet(Long userId, String balance) {
        Wallet w = new Wallet();
        w.setUserId(userId);
        w.setBalance(new BigDecimal(balance));
        return w;
    }

    private static UserUpgrade userUpgrade(Long userId, Long upgradeId, int level) {
        UserUpgrade uu = new UserUpgrade();
        uu.setUserId(userId);
        uu.setUpgradeId(upgradeId);
        uu.setLevel(level);
        return uu;
    }
}