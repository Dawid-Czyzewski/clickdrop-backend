package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.economy.domain.upgrade.Upgrade;
import pl.clickdrop.economy.domain.upgrade.UpgradeRepository;
import pl.clickdrop.economy.domain.upgrade.UserUpgrade;
import pl.clickdrop.economy.domain.upgrade.UserUpgradeRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClickRewardService {

    static final BigDecimal BASE_CLICK_REWARD = BigDecimal.ONE;
    private final UserUpgradeRepository userUpgradeRepository;
    private final UpgradeRepository upgradeRepository;

    @Transactional(readOnly = true)
    public BigDecimal calculateReward(Long userId) {
        List<UserUpgrade> owned = userUpgradeRepository.findByUserId(userId);

        if (owned.isEmpty()) {
            return BASE_CLICK_REWARD;
        }

        Map<Long, Upgrade> catalogById = upgradeRepository.findAllById(
                owned.stream().map(UserUpgrade::getUpgradeId).toList()
        ).stream().collect(Collectors.toMap(Upgrade::getId, Function.identity()));

        BigDecimal bonus = BigDecimal.ZERO;
        for (UserUpgrade userUpgrade : owned) {
            if (userUpgrade.getLevel() == null || userUpgrade.getLevel() <= 0) {
                continue;
            }

            Upgrade catalog = catalogById.get(userUpgrade.getUpgradeId());
            if (catalog == null || !catalog.isActive()) {
                continue;
            }

            BigDecimal level = BigDecimal.valueOf(userUpgrade.getLevel());
            bonus = bonus.add(catalog.getClickPower().multiply(level));
        }

        return  BASE_CLICK_REWARD.add(bonus);
    }
}
