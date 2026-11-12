package pl.clickdrop.economy.domain.collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionItemId implements Serializable {
    private Long collectionId;
    private Long itemId;
}
