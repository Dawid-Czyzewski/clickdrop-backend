package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.economy.api.dto.RecentDropResponse;
import pl.clickdrop.economy.domain.recent.RecentDrop;
import pl.clickdrop.economy.domain.recent.RecentDropRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecentDropsService {

    static final int DEFAULT_LIMIT = 20;
    static final int MAX_LIMIT = 50;

    private final RecentDropRepository recentDropRepository;

    @Transactional(readOnly = true)
    public List<RecentDropResponse> listRecent(Integer limit) {
        int size = normalizeLimit(limit);
        return recentDropRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, size))
                .stream().map(this::toResponse)
                .toList();
    }

    private static int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
             return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private RecentDropResponse toResponse(RecentDrop row) {
        return new RecentDropResponse(
                row.getItemCode(),
                row.getItemDisplayName(),
                row.getRarity(),
                row.getExterior(),
                row.isStatTrack(),
                row.getSource(),
                row.getCreatedAt(),
                row.getCaseId(),
                row.getCaseCode()
        );
    }
}
