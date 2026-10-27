package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.recent.RecentDrop;
import pl.clickdrop.economy.domain.recent.RecentDropRepository;
import pl.clickdrop.economy.domain.recent.RecentDropSource;

@Component
@RequiredArgsConstructor
public class RecentDropRecorder {

    private final RecentDropRepository recentDropRepository;

    public void record(
            Long userId,
            Item item,
            ItemExterior exterior,
            boolean statTrack,
            RecentDropSource source,
            Long caseId,
            String caseCode
    ) {
        RecentDrop row = new RecentDrop();
        row.setUserId(userId);
        row.setItemCode(item.getCode());
        row.setItemDisplayName(item.getDisplayName());
        row.setRarity(item.getRarity());
        row.setExterior(exterior);
        row.setStatTrack(statTrack);
        row.setSource(source);
        if (source == RecentDropSource.CASE) {
            row.setCaseId(caseId);
            row.setCaseCode(caseCode);
        } else {
            row.setCaseId(null);
            row.setCaseCode(null);
        }

        recentDropRepository.save(row);
    }
}
