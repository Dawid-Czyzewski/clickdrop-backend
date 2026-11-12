package pl.clickdrop.economy.domain.collection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserCollectionClaimRepository extends JpaRepository<UserCollectionClaim, UserCollectionClaimId> {
    List<UserCollectionClaim> findByUserIdAndCollectionIdIn(Long userId, Collection<Long> collectionIds);

    boolean existsByUserIdAndCollectionId(Long userId, Long collectionId);
}
