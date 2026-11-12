package pl.clickdrop.economy.domain.collection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CollectionItemRepository extends JpaRepository<CollectionItem, CollectionItemId> {
    List<CollectionItem> findByCollectionIdOrderByItemIdAsc(Long collectionId);

    List<CollectionItem> findByCollectionIdIn(Collection<Long> collectionIds);

    long countByCollectionId(Long collectionId);
}
