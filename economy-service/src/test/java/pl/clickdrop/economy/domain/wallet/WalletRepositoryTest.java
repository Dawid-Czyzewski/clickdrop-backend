package pl.clickdrop.economy.domain.wallet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WalletRepositoryTest {

    @Autowired
    WalletRepository walletRepository;

    @Test
    void shouldSaveWallet_withDefaultBalanceZero_andTimestamps() {
        Wallet wallet = new Wallet();
        wallet.setUserId(42L);

        Wallet saved = walletRepository.saveAndFlush(wallet);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(42L);
        assertThat(saved.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindWalletByUserId() {
        Wallet wallet = new Wallet();
        wallet.setUserId(100L);
        walletRepository.saveAndFlush(wallet);

        Wallet found = walletRepository.findByUserId(100L).orElseThrow();

        assertThat(found.getUserId()).isEqualTo(100L);
        assertThat(found.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(walletRepository.existsByUserId(100L)).isTrue();
        assertThat(walletRepository.existsByUserId(999L)).isFalse();
    }

    @Test
    void shouldRejectSecondWalletForSameUserId() {
        Wallet first = new Wallet();
        first.setUserId(7L);
        walletRepository.saveAndFlush(first);

        Wallet second = new Wallet();
        second.setUserId(7L);

        assertThatThrownBy(() -> walletRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}