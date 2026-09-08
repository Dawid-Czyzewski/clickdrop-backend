package pl.clickdrop.economy.application;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.clickdrop.economy.api.dto.MyUpgradeResponse;
import pl.clickdrop.economy.api.dto.UpgradeResponse;
import pl.clickdrop.economy.domain.upgrade.Upgrade;
import pl.clickdrop.economy.domain.upgrade.UpgradeRepository;
import pl.clickdrop.economy.domain.upgrade.UserUpgrade;
import pl.clickdrop.economy.domain.upgrade.UserUpgradeRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpgradeService {

    private final UpgradeRepository repository;
    private final UserUpgradeRepository userUpgradeRepository;

    @Transactional(readOnly = true)
    public List<UpgradeResponse> listActive()  {
        return repository.findByActiveTrueOrderByTierAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyUpgradeResponse> listMine(Long userId) {
        Map<Long, Integer> levelByUpgradeId = userUpgradeRepository.findByUserId(userId)
                .stream()
                .collect(Collectors.toMap(
                   UserUpgrade::getUpgradeId,
                   uu -> uu.getLevel() == null ? 0 : uu.getLevel()
                ));

        return repository.findByActiveTrueOrderByTierAsc()
                .stream()
                .map(u -> toMineResponse(u, levelByUpgradeId.getOrDefault(u.getId(), 0)))
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

    private MyUpgradeResponse toMineResponse(Upgrade u, int level) {
        return new MyUpgradeResponse(
                u.getId(),
                u.getCode(),
                u.getTier(),
                u.getPrice(),
                u.getClickPower(),
                level,
                UpgradePricing.nextCost(u.getPrice(), level)
        );
    }
}
