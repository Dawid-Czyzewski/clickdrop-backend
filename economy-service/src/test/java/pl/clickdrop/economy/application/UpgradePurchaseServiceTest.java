package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.BuyUpgradeResponse;
import pl.clickdrop.economy.application.client.AuthClient;
import pl.clickdrop.economy.application.exception.InsufficientFundsException;
import pl.clickdrop.economy.application.exception.UpgradeLevelTooLowException;
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
    @Mock AuthClient authClient;

    @InjectMocks
    UpgradePurchaseService purchaseService;

    @Test
    void buy_firstTime_createsLevel1_andChargesBasePrice() {
        Upgrade catalog = catalog(1L, "BETTER_MOUSE", "50", 1);
        Wallet wallet = wallet(15L, "100");

        when(upgradeRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(authClient.getLevel(15L)).thenReturn(1);
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
        verify(authClient).getLevel(15L);
    }

    @Test
    void buy_ok_whenUserLevelEqualsMinLevel() {
        Upgrade catalog = catalog(2L, "GAMING_MOUSE", "200", 5);
        Wallet wallet = wallet(15L, "500");

        when(upgradeRepository.findById(2L)).thenReturn(Optional.of(catalog));
        when(authClient.getLevel(15L)).thenReturn(5);
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(userUpgradeRepository.findByUserIdAndUpgradeId(15L, 2L)).thenReturn(Optional.empty());
        when(walletRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userUpgradeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BuyUpgradeResponse res = purchaseService.buy(15L, 2L);

        assertThat(res.level()).isEqualTo(1);
        assertThat(res.cost()).isEqualByComparingTo("200");
        verify(walletRepository).save(any());
    }

    @Test
    void buy_throws403_whenUserLevelBelowMin_noSideEffects() {
        Upgrade catalog = catalog(2L, "GAMING_MOUSE", "200", 5);

        when(upgradeRepository.findById(2L)).thenReturn(Optional.of(catalog));
        when(authClient.getLevel(15L)).thenReturn(4);

        assertThatThrownBy(() -> purchaseService.buy(15L, 2L))
                .isInstanceOf(UpgradeLevelTooLowException.class)
                .satisfies(ex -> {
                    UpgradeLevelTooLowException e = (UpgradeLevelTooLowException) ex;
                    assertThat(e.getRequiredLevel()).isEqualTo(5);
                    assertThat(e.getUserLevel()).isEqualTo(4);
                    assertThat(e.getUpgradeId()).isEqualTo(2L);
                });

        verify(walletRepository, never()).findByUserId(any());
        verify(walletRepository, never()).save(any());
        verify(userUpgradeRepository, never()).save(any());
    }

    @Test
    void buy_atLevel1_chargesDoubleBasePrice() {
        Upgrade catalog = catalog(1L, "BETTER_MOUSE", "50", 1);
        Wallet wallet = wallet(15L, "150");
        UserUpgrade owned = userUpgrade(15L, 1L, 1);

        when(upgradeRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(authClient.getLevel(15L)).thenReturn(1);
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(userUpgradeRepository.findByUserIdAndUpgradeId(15L, 1L)).thenReturn(Optional.of(owned));
        when(walletRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userUpgradeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BuyUpgradeResponse res = purchaseService.buy(15L, 1L);

        assertThat(res.level()).isEqualTo(2);
        assertThat(res.cost()).isEqualByComparingTo("100");
        assertThat(res.balance()).isEqualByComparingTo("50");
    }

    @Test
    void buy_atLevel2_chargesTripleBasePrice_andIncrementsLevel() {
        Upgrade catalog = catalog(1L, "BETTER_MOUSE", "50", 1);
        Wallet wallet = wallet(15L, "200");
        UserUpgrade owned = userUpgrade(15L, 1L, 2);

        when(upgradeRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(authClient.getLevel(15L)).thenReturn(1);
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(userUpgradeRepository.findByUserIdAndUpgradeId(15L, 1L)).thenReturn(Optional.of(owned));
        when(walletRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userUpgradeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BuyUpgradeResponse res = purchaseService.buy(15L, 1L);

        assertThat(res.level()).isEqualTo(3);
        assertThat(res.cost()).isEqualByComparingTo("150");
        assertThat(res.balance()).isEqualByComparingTo("50");
    }

    @Test
    void buy_throws_whenInsufficientFunds_atLevel0_andDoesNotSave() {
        Upgrade catalog = catalog(1L, "BETTER_MOUSE", "50", 1);
        Wallet wallet = wallet(15L, "10");

        when(upgradeRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(authClient.getLevel(15L)).thenReturn(1);
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(userUpgradeRepository.findByUserIdAndUpgradeId(15L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.buy(15L, 1L))
                .isInstanceOf(InsufficientFundsException.class);

        verify(walletRepository, never()).save(any());
        verify(userUpgradeRepository, never()).save(any());
        assertThat(wallet.getBalance()).isEqualByComparingTo("10");
    }

    @Test
    void buy_throws_whenInsufficientFunds_atLevel1_scaledCost() {
        Upgrade catalog = catalog(1L, "BETTER_MOUSE", "50", 1);
        Wallet wallet = wallet(15L, "50");
        UserUpgrade owned = userUpgrade(15L, 1L, 1);

        when(upgradeRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(authClient.getLevel(15L)).thenReturn(1);
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(userUpgradeRepository.findByUserIdAndUpgradeId(15L, 1L)).thenReturn(Optional.of(owned));

        assertThatThrownBy(() -> purchaseService.buy(15L, 1L))
                .isInstanceOf(InsufficientFundsException.class);

        verify(walletRepository, never()).save(any());
        verify(userUpgradeRepository, never()).save(any());
        assertThat(owned.getLevel()).isEqualTo(1);
        assertThat(wallet.getBalance()).isEqualByComparingTo("50");
    }

    @Test
    void buy_throws_whenUpgradeMissing() {
        when(upgradeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.buy(15L, 99L))
                .isInstanceOf(UpgradeNotFoundException.class);

        verifyNoInteractions(walletRepository, userUpgradeRepository, authClient);
    }

    private static Upgrade catalog(Long id, String code, String price, int minLevel) {
        Upgrade u = new Upgrade();
        u.setId(id);
        u.setCode(code);
        u.setPrice(new BigDecimal(price));
        u.setMinLevel(minLevel);
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
