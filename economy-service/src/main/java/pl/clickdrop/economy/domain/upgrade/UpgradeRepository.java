package pl.clickdrop.economy.domain.upgrade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UpgradeRepository extends JpaRepository<Upgrade, Long> {
    List<Upgrade> findByActiveTrueOrderByTierAsc();
    Optional<Upgrade> findByCode(String code);
}
