package pl.clickdrop.economy.domain.daily;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "daily_drops")
public class DailyDrop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", nullable = false, unique = true)
    private Long itemId;

    @Column(nullable = false)
    private Integer weight;
}
