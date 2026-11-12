package pl.clickdrop.economy.domain.collection;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "collection_items")
@IdClass(CollectionItemId.class)
public class CollectionItem {

    @Id
    @Column(name = "collection_id", nullable = false)
    private Long collectionId;

    @Id
    @Column(name = "item_id", nullable = false)
    private Long itemId;
}
