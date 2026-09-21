package pl.clickdrop.economy.domain.cases;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameCaseRepository extends JpaRepository<GameCase, Long> {
    List<GameCase> findByActiveTrueOrderBySortOrderAsc();
    Optional<GameCase> findByCode(String code);
}
