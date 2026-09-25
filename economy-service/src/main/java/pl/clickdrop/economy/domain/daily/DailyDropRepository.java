package pl.clickdrop.economy.domain.daily;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyDropRepository extends JpaRepository<DailyDrop, Long> {
    List<DailyDrop> findAll();
}
