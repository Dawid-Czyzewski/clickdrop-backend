package pl.clickdrop.economy.domain.collection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByActiveTrueOrderBySortOrderAscIdAsc();
    Optional<Collection> findByCodeAndActiveTrue(String code);
}
