package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.UpgradeResponse;
import pl.clickdrop.economy.domain.upgrade.Upgrade;
import pl.clickdrop.economy.domain.upgrade.UpgradeRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpgradeServiceTest {

    @Mock
    UpgradeRepository repository;

    @InjectMocks
    UpgradeService upgradeService;

    @Test
    void listActive_mapsEntitiesToResponse_withCodeForI18n() {
        Upgrade mouse = new Upgrade();
        mouse.setId(1L);
        mouse.setCode("BETTER_MOUSE");
        mouse.setName("Better Mouse");
        mouse.setDescription("EN seed text");
        mouse.setTier(1);
        mouse.setPrice(new BigDecimal("50.00"));
        mouse.setClickPower(new BigDecimal("1.00"));
        mouse.setActive(true);

        when(repository.findByActiveTrueOrderByTierAsc()).thenReturn(List.of(mouse));

        List<UpgradeResponse> result = upgradeService.listActive();

        assertThat(result).hasSize(1);
        UpgradeResponse dto = result.getFirst();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.code()).isEqualTo("BETTER_MOUSE");
        assertThat(dto.tier()).isEqualTo(1);
        assertThat(dto.price()).isEqualByComparingTo("50.00");
        assertThat(dto.clickPower()).isEqualByComparingTo("1.00");

        verify(repository).findByActiveTrueOrderByTierAsc();
    }

    @Test
    void listActive_returnsEmptyList_whenNoneActive() {
        when(repository.findByActiveTrueOrderByTierAsc()).thenReturn(List.of());

        assertThat(upgradeService.listActive()).isEmpty();
    }
}