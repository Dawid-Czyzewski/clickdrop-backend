package pl.clickdrop.economy.domain.collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCollectionClaimId implements Serializable {
    private Long userId;
    private Long collectionId;
}
