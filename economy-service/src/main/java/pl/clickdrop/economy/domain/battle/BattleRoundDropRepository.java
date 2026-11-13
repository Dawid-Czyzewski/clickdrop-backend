package pl.clickdrop.economy.domain.battle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BattleRoundDropRepository extends JpaRepository<BattleRoundDrop, Long> {
    List<BattleRoundDrop> findByBattleIdOrderByRoundIndexAscSideAsc(Long battleId);
}
