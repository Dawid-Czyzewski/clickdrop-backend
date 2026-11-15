package pl.clickdrop.inventory.domain.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserInventoryItemRepository extends JpaRepository<UserInventoryItem, Long> {
    List<UserInventoryItem> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<UserInventoryItem> findByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT DISTINCT ui.itemId FROM UserInventoryItem ui
            WHERE ui.userId = :userId AND ui.itemId IN :itemIds
            """)
    List<Long> findDistinctItemIdsOwned(
            @Param("userId") Long userId,
            @Param("itemIds") Collection<Long> itemIds
    );
}
