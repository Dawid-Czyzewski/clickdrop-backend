package pl.clickdrop.economy.domain.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserInventoryItemRepository extends JpaRepository<UserInventoryItem, Long> {
    List<UserInventoryItem> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<UserInventoryItem> findByIdAndUserId(Long id, Long userId);
}
