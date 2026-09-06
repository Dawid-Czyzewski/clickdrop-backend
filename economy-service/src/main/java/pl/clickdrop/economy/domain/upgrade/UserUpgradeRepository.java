package pl.clickdrop.economy.domain.upgrade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserUpgradeRepository extends JpaRepository<UserUpgrade, Long> {
    List<UserUpgrade> findByUserId(Long userId);
    Optional<UserUpgrade> findByUserIdAndUpgradeId(Long userId, Long upgradeId);
    boolean existsByUserIdAndUpgradeId(Long userId, Long upgradeId);
}
