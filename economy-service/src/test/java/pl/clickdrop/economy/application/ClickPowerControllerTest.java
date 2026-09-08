package pl.clickdrop.economy.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import pl.clickdrop.economy.api.dto.ClickPowerResponse;
import pl.clickdrop.economy.application.ClickRewardService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClickPowerControllerTest {

    @Mock
    ClickRewardService clickRewardService;

    @InjectMocks
    ClickPowerController controller;

    @Test
    void getClickPower_returnsCalculatedReward_forJwtUser() {
        when(clickRewardService.calculateReward(15L)).thenReturn(new BigDecimal("4"));

        var auth = new UsernamePasswordAuthenticationToken(15L, null);

        ResponseEntity<ClickPowerResponse> response = controller.getClickPower(auth);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().rewardPerClick()).isEqualByComparingTo("4");
        verify(clickRewardService).calculateReward(15L);
    }
}