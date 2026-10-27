package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;
import pl.clickdrop.economy.domain.recent.RecentDrop;
import pl.clickdrop.economy.domain.recent.RecentDropRepository;
import pl.clickdrop.economy.domain.recent.RecentDropSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RecentDropRecorderTest {

    @Mock RecentDropRepository recentDropRepository;

    @InjectMocks
    RecentDropRecorder recentDropRecorder;

    @Test
    void record_case_persistsCaseIdAndCode() {
        Item item = item();

        recentDropRecorder.record(
                15L,
                item,
                ItemExterior.FIELD_TESTED,
                false,
                RecentDropSource.CASE,
                10L,
                "REVOLUTION_CASE"
        );

        ArgumentCaptor<RecentDrop> captor = ArgumentCaptor.forClass(RecentDrop.class);
        verify(recentDropRepository).save(captor.capture());
        RecentDrop saved = captor.getValue();
        assertThat(saved.getSource()).isEqualTo(RecentDropSource.CASE);
        assertThat(saved.getCaseId()).isEqualTo(10L);
        assertThat(saved.getCaseCode()).isEqualTo("REVOLUTION_CASE");
    }

    @Test
    void record_daily_leavesCaseRefNull() {
        Item item = item();

        recentDropRecorder.record(
                15L,
                item,
                ItemExterior.MINIMAL_WEAR,
                true,
                RecentDropSource.DAILY,
                99L,
                "SHOULD_IGNORE"
        );

        ArgumentCaptor<RecentDrop> captor = ArgumentCaptor.forClass(RecentDrop.class);
        verify(recentDropRepository).save(captor.capture());
        RecentDrop saved = captor.getValue();
        assertThat(saved.getSource()).isEqualTo(RecentDropSource.DAILY);
        assertThat(saved.getCaseId()).isNull();
        assertThat(saved.getCaseCode()).isNull();
    }

    private static Item item() {
        Item item = new Item();
        item.setId(3L);
        item.setCode("AK_REDLINE");
        item.setDisplayName("AK | Redline");
        item.setRarity(ItemRarity.RESTRICTED);
        item.setActive(true);
        return item;
    }
}
