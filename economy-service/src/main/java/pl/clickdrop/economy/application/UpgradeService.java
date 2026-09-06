package pl.clickdrop.economy.application;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.clickdrop.economy.api.dto.UpgradeResponse;
import pl.clickdrop.economy.domain.upgrade.Upgrade;
import pl.clickdrop.economy.domain.upgrade.UpgradeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UpgradeService {

    private final UpgradeRepository repository;

    @Transactional(readOnly = true)
    public List<UpgradeResponse> listActive()  {
        return repository.findByActiveTrueOrderByTierAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UpgradeResponse toResponse(Upgrade u) {
        return new UpgradeResponse(
                u.getId(),
                u.getCode(),
                u.getName(),
                u.getDescription(),
                u.getTier(),
                u.getPrice(),
                u.getClickPower()
        );
    }
}
