package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.application.exception.WalletAlreadyExistsException;
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    WalletRepository repository;

    @InjectMocks
    WalletService walletService;

    @Test
    void createWalletForUser_createsWallet_withZeroBalance() {
        when(repository.existsByUserId(42L)).thenReturn(false);
        when(repository.save(any(Wallet.class))).thenAnswer(inv -> {
            Wallet w = inv.getArgument(0);
            w.setId(1L);
            return w;
        });

        Wallet result = walletService.createWalletForUser(42L);

        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        verify(repository).save(captor.capture());

        Wallet saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(42L);
        assertThat(saved.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void createWalletForUser_throws_whenWalletAlreadyExists() {
        when(repository.existsByUserId(42L)).thenReturn(true);

        assertThatThrownBy(() -> walletService.createWalletForUser(42L))
                .isInstanceOf(WalletAlreadyExistsException.class);

        verify(repository, never()).save(any());
    }
}