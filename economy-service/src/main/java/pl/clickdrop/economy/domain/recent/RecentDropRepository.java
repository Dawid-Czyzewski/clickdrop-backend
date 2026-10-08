package pl.clickdrop.economy.domain.recent;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface RecentDropRepository extends JpaRepository<RecentDrop, Long> {
    List<RecentDrop> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<RecentDrop> findByUserIdAndSourceOrderByCreatedAtDesc(
            Long userId,
            RecentDropSource source,
            Pageable pageable
    );
}
