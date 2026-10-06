package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.IdleCollectResponse;
import pl.clickdrop.economy.application.exception.WalletNotFoundException;
import pl.clickdrop.economy.domain.upgrade.Upgrade;
import pl.clickdrop.economy.domain.upgrade.UpgradeRepository;
import pl.clickdrop.economy.domain.upgrade.UserUpgrade;
import pl.clickdrop.economy.domain.upgrade.UserUpgradeRepository;
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdleIncomeServiceTest {

    @Mock
    WalletRepository walletRepository;

    @Mock
    UserUpgradeRepository userUpgradeRepository;

    @Mock
    UpgradeRepository upgradeRepository;

    @InjectMocks
    IdleIncomeService idleIncomeService;

    @Test
    void collect_returnsZero_whenNoUpgrades() {
        Wallet wallet = wallet(15L, "100", Instant.now().minusSeconds(60));
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(userUpgradeRepository.findByUserId(15L)).thenReturn(List.of());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        IdleCollectResponse res = idleIncomeService.collect(15L);

        assertThat(res.earned()).isEqualByComparingTo("0.00");
        assertThat(res.balance()).isEqualByComparingTo("100");
        assertThat(res.incomePerSecond()).isEqualByComparingTo("0.0000");
        assertThat(wallet.getBalance()).isEqualByComparingTo("100");
        verify(walletRepository).save(wallet);
    }

    @Test
    void collect_returnsZero_whenOnlyClickUpgrade() {
        Wallet wallet = wallet(15L, "50", Instant.now().minusSeconds(30));
        UserUpgrade uu = userUpgrade(15L, 1L, 3);
        Upgrade mouse = catalog(1L, "1.00", "0");

        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(userUpgradeRepository.findByUserId(15L)).thenReturn(List.of(uu));
        when(upgradeRepository.findAllById(anyCollection())).thenReturn(List.of(mouse));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        IdleCollectResponse res = idleIncomeService.collect(15L);

        assertThat(res.earned()).isEqualByComparingTo("0.00");
        assertThat(res.incomePerSecond()).isEqualByComparingTo("0.0000");
        assertThat(res.balance()).isEqualByComparingTo("50");
    }

    @Test
    void collect_botFarmLevel1_for10Seconds_earnsOne() {
        Wallet wallet = wallet(15L, "100", Instant.now().minusSeconds(10));
        UserUpgrade uu = userUpgrade(15L, 10L, 1);
        Upgrade botFarm = catalog(10L, "0", "0.1000");

        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(userUpgradeRepository.findByUserId(15L)).thenReturn(List.of(uu));
        when(upgradeRepository.findAllById(anyCollection())).thenReturn(List.of(botFarm));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        IdleCollectResponse res = idleIncomeService.collect(15L);

        assertThat(res.incomePerSecond()).isEqualByComparingTo("0.1000");
        assertThat(res.earned()).isEqualByComparingTo("1.00");
        assertThat(res.balance()).isEqualByComparingTo("101.00");
    }

    @Test
    void collect_sumsMultipleIdleLevels() {
        Wallet wallet = wallet(15L, "10", Instant.now().minusSeconds(10));
        UserUpgrade bot = userUpgrade(15L, 10L, 2);
        UserUpgrade afk = userUpgrade(15L, 11L, 1);
        Upgrade botFarm = catalog(10L, "0", "0.1000");
        Upgrade afkScript = catalog(11L, "0", "0.5000");

        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(userUpgradeRepository.findByUserId(15L)).thenReturn(List.of(bot, afk));
        when(upgradeRepository.findAllById(anyCollection())).thenReturn(List.of(botFarm, afkScript));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        IdleCollectResponse res = idleIncomeService.collect(15L);

        assertThat(res.incomePerSecond()).isEqualByComparingTo("0.7000");
        assertThat(res.earned()).isEqualByComparingTo("7.00");
        assertThat(res.balance()).isEqualByComparingTo("17.00");
    }

    @Test
    void collect_ignoresLevelZeroIdle() {
        Wallet wallet = wallet(15L, "20", Instant.now().minusSeconds(100));
        UserUpgrade uu = userUpgrade(15L, 10L, 0);
        Upgrade botFarm = catalog(10L, "0", "0.1000");

        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(userUpgradeRepository.findByUserId(15L)).thenReturn(List.of(uu));
        when(upgradeRepository.findAllById(anyCollection())).thenReturn(List.of(botFarm));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        IdleCollectResponse res = idleIncomeService.collect(15L);

        assertThat(res.earned()).isEqualByComparingTo("0.00");
        assertThat(res.balance()).isEqualByComparingTo("20");
    }

    @Test
    void collect_capsDeltaAt8Hours() {
        Wallet wallet = wallet(15L, "0", Instant.now().minusSeconds(2L * 24 * 3600));
        UserUpgrade uu = userUpgrade(15L, 10L, 1);
        Upgrade botFarm = catalog(10L, "0", "0.1000");

        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(userUpgradeRepository.findByUserId(15L)).thenReturn(List.of(uu));
        when(upgradeRepository.findAllById(anyCollection())).thenReturn(List.of(botFarm));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        IdleCollectResponse res = idleIncomeService.collect(15L);

        assertThat(res.earned()).isEqualByComparingTo("2880.00");
        assertThat(res.balance()).isEqualByComparingTo("2880.00");
    }

    @Test
    void collect_updatesLastIdleCollectedAt_evenWhenEarnedZero() {
        Instant past = Instant.now().minusSeconds(5);
        Wallet wallet = wallet(15L, "100", past);
        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(userUpgradeRepository.findByUserId(15L)).thenReturn(List.of());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        idleIncomeService.collect(15L);

        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(captor.capture());
        assertThat(captor.getValue().getLastIdleCollectedAt()).isAfter(past);
    }

    @Test
    void collect_throws_whenWalletMissing() {
        when(walletRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> idleIncomeService.collect(99L))
                .isInstanceOf(WalletNotFoundException.class);
    }

    @Test
    void status_returnsPendingWithoutChangingWallet() {
        Instant last = Instant.now().minusSeconds(10);
        Wallet wallet = wallet(15L, "100", last);
        UserUpgrade uu = userUpgrade(15L, 1L, 1);
        Upgrade farm = catalog(1L, "0", "0.1000");

        when(walletRepository.findByUserId(15L)).thenReturn(Optional.of(wallet));
        when(userUpgradeRepository.findByUserId(15L)).thenReturn(List.of(uu));
        when(upgradeRepository.findAllById(anyCollection())).thenReturn(List.of(farm));

        var res = idleIncomeService.status(15L);

        assertThat(res.pending()).isEqualByComparingTo("1.00");
        assertThat(res.incomePerSecond()).isEqualByComparingTo("0.1000");
        assertThat(res.lastIdleCollectedAt()).isEqualTo(last);
        assertThat(wallet.getBalance()).isEqualByComparingTo("100");
        assertThat(wallet.getLastIdleCollectedAt()).isEqualTo(last);
    }

    private static Wallet wallet(Long userId, String balance, Instant lastIdle) {
        Wallet w = new Wallet();
        w.setUserId(userId);
        w.setBalance(new BigDecimal(balance));
        w.setLastIdleCollectedAt(lastIdle);
        return w;
    }

    private static UserUpgrade userUpgrade(Long userId, Long upgradeId, int level) {
        UserUpgrade uu = new UserUpgrade();
        uu.setUserId(userId);
        uu.setUpgradeId(upgradeId);
        uu.setLevel(level);
        return uu;
    }

    private static Upgrade catalog(Long id, String clickPower, String incomePerSecond) {
        Upgrade u = new Upgrade();
        u.setId(id);
        u.setClickPower(new BigDecimal(clickPower));
        u.setIncomePerSecond(new BigDecimal(incomePerSecond));
        u.setActive(true);
        return u;
    }
}