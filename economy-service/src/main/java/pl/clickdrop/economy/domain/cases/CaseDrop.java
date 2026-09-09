package pl.clickdrop.economy.domain.cases;

import jakarta.persistence.*;
import lombok.Data;
import pl.clickdrop.economy.domain.item.Item;

@Data
@Entity
@Table(
        name = "case_drops",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_case_drops_case_item",
                        columnNames = {"case_id", "item_id"}
                )
        }
)
public class CaseDrop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Integer weight;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private Item item;
}
