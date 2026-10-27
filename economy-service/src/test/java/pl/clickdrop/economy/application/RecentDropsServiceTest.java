package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import pl.clickdrop.economy.api.dto.RecentDropResponse;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;
import pl.clickdrop.economy.domain.recent.RecentDrop;
import pl.clickdrop.economy.domain.recent.RecentDropRepository;
import pl.clickdrop.economy.domain.recent.RecentDropSource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecentDropsServiceTest {

    @Mock RecentDropRepository recentDropRepository;

    @InjectMocks
    RecentDropsService recentDropsService;

    @Test
    void listRecent_mapsRows_andUsesDefaultLimit() {
        RecentDrop row = drop("AK_REDLINE", RecentDropSource.CASE);
        when(recentDropRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .thenReturn(List.of(row));

        List<RecentDropResponse> result = recentDropsService.listRecent(null);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(recentDropRepository).findAllByOrderByCreatedAtDesc(pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(20);

        assertThat(result).hasSize(1);
        RecentDropResponse dto = result.getFirst();
        assertThat(dto.itemCode()).isEqualTo("AK_REDLINE");
        assertThat(dto.displayName()).isEqualTo("AK REDLINE");
        assertThat(dto.rarity()).isEqualTo(ItemRarity.RESTRICTED);
        assertThat(dto.exterior()).isEqualTo(ItemExterior.FIELD_TESTED);
        assertThat(dto.statTrack()).isFalse();
        assertThat(dto.source()).isEqualTo(RecentDropSource.CASE);
        assertThat(dto.caseId()).isEqualTo(7L);
        assertThat(dto.caseCode()).isEqualTo("REVOLUTION_CASE");
    }

    @Test
    void listRecent_mapsDailyWithoutCaseRef() {
        RecentDrop row = drop("AK_REDLINE", RecentDropSource.DAILY);
        when(recentDropRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .thenReturn(List.of(row));

        RecentDropResponse dto = recentDropsService.listRecent(5).getFirst();

        assertThat(dto.source()).isEqualTo(RecentDropSource.DAILY);
        assertThat(dto.caseId()).isNull();
        assertThat(dto.caseCode()).isNull();
    }

    @Test
    void listRecent_clampsLimitToMax() {
        when(recentDropRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .thenReturn(List.of());

        recentDropsService.listRecent(999);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(recentDropRepository).findAllByOrderByCreatedAtDesc(pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(50);
    }

    private static RecentDrop drop(String code, RecentDropSource source) {
        RecentDrop row = new RecentDrop();
        row.setUserId(15L);
        row.setItemCode(code);
        row.setItemDisplayName(code.replace('_', ' '));
        row.setRarity(ItemRarity.RESTRICTED);
        row.setExterior(ItemExterior.FIELD_TESTED);
        row.setStatTrack(false);
        row.setSource(source);
        row.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        if (source == RecentDropSource.CASE) {
            row.setCaseId(7L);
            row.setCaseCode("REVOLUTION_CASE");
        }
        return row;
    }
}
