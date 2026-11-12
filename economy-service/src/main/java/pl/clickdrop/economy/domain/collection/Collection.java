package pl.clickdrop.economy.domain.collection;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "collections")
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128, unique = true)
    private String code;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName = "";

    @Column(name = "reward_cd", nullable = false, precision = 19, scale = 2)
    private BigDecimal rewardCd;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private boolean active = true;
}
