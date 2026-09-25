package pl.clickdrop.economy.domain.daily;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DailyClaimRepository extends JpaRepository<DailyClaim, Long> {
    Optional<DailyClaim> findByUserId(Long userId);
}
