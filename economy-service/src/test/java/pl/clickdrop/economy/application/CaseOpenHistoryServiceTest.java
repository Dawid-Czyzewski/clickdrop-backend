package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import pl.clickdrop.economy.api.dto.CaseOpenHistoryItemResponse;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;
import pl.clickdrop.economy.domain.recent.RecentDrop;
import pl.clickdrop.economy.domain.recent.RecentDropRepository;
import pl.clickdrop.economy.domain.recent.RecentDropSource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseOpenHistoryServiceTest {

    @Mock RecentDropRepository recentDropRepository;

    @InjectMocks
    CaseOpenHistoryService caseOpenHistoryService;

    @Test
    void listMine_returnsOnlyMappedCaseOpens_forUser() {
        RecentDrop row = drop(15L, "AK_REDLINE");
        when(recentDropRepository.findByUserIdAndSourceOrderByCreatedAtDesc(
                eq(15L), eq(RecentDropSource.CASE), any(Pageable.class)))
                .thenReturn(List.of(row));

        List<CaseOpenHistoryItemResponse> result = caseOpenHistoryService.listMine(15L, null);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(recentDropRepository).findByUserIdAndSourceOrderByCreatedAtDesc(
                eq(15L), eq(RecentDropSource.CASE), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(20);

        assertThat(result).hasSize(1);
        CaseOpenHistoryItemResponse dto = result.getFirst();
        assertThat(dto.itemCode()).isEqualTo("AK_REDLINE");
        assertThat(dto.displayName()).isEqualTo("AK REDLINE");
        assertThat(dto.rarity()).isEqualTo(ItemRarity.RESTRICTED);
        assertThat(dto.exterior()).isEqualTo(ItemExterior.FIELD_TESTED);
        assertThat(dto.statTrack()).isFalse();
    }

    @Test
    void listMine_clampsLimitToMax() {
        when(recentDropRepository.findByUserIdAndSourceOrderByCreatedAtDesc(
                eq(15L), eq(RecentDropSource.CASE), any(Pageable.class)))
                .thenReturn(List.of());

        caseOpenHistoryService.listMine(15L, 999);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(recentDropRepository).findByUserIdAndSourceOrderByCreatedAtDesc(
                eq(15L), eq(RecentDropSource.CASE), pageable.capture());
        assertThat(pageable.getValue().getPageSize()).isEqualTo(50);
    }

    private static RecentDrop drop(Long userId, String code) {
        RecentDrop row = new RecentDrop();
        row.setUserId(userId);
        row.setItemCode(code);
        row.setItemDisplayName(code.replace('_', ' '));
        row.setRarity(ItemRarity.RESTRICTED);
        row.setExterior(ItemExterior.FIELD_TESTED);
        row.setStatTrack(false);
        row.setSource(RecentDropSource.CASE);
        row.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        return row;
    }
}