package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pl.clickdrop.economy.api.dto.CaseOpenHistoryItemResponse;
import pl.clickdrop.economy.domain.recent.RecentDrop;
import pl.clickdrop.economy.domain.recent.RecentDropRepository;
import pl.clickdrop.economy.domain.recent.RecentDropSource;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseOpenHistoryService {

    static final int DEFAULT_LIMIT = 20;
    static final int MAX_LIMIT = 50;

    private final RecentDropRepository recentDropRepository;

    public List<CaseOpenHistoryItemResponse> listMine(Long userId, Integer limit) {
        int size = normalizeLimit(limit);

        return recentDropRepository
                .findByUserIdAndSourceOrderByCreatedAtDesc(
                        userId,
                        RecentDropSource.CASE,
                        PageRequest.of(0, size)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private static int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private CaseOpenHistoryItemResponse toResponse(RecentDrop row) {
        return new CaseOpenHistoryItemResponse(
                row.getItemCode(),
                row.getItemDisplayName(),
                row.getRarity(),
                row.getExterior(),
                row.isStatTrack(),
                row.getCreatedAt()
        );
    }
}
