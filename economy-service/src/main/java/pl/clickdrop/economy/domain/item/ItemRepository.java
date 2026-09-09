package pl.clickdrop.economy.domain.item;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByActiveTrueOrderByIdAsc();
    Optional<Item> findByCode(String code);
}
