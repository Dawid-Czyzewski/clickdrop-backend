package pl.clickdrop.economy.domain.collection;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "user_collection_claims")
@IdClass(UserCollectionClaimId.class)
public class UserCollectionClaim {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "collection_id", nullable = false)
    private Long collectionId;

    @Column(name = "claimed_at", nullable = false)
    private Instant claimedAt;
}
