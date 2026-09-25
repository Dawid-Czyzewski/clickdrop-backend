CREATE TABLE daily_claims (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    claimed_at   TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_daily_claims_user_id UNIQUE (user_id)
);

CREATE TABLE daily_drops (
    id           BIGSERIAL PRIMARY KEY,
    item_id      BIGINT       NOT NULL,
    weight       INT          NOT NULL,
    CONSTRAINT fk_daily_drops_item FOREIGN KEY (item_id) REFERENCES items (id),
    CONSTRAINT uk_daily_drops_item UNIQUE (item_id),
    CONSTRAINT ck_daily_drops_weight CHECK (weight > 0)
);

CREATE INDEX idx_daily_claims_user_id ON daily_claims (user_id);

INSERT INTO daily_drops (item_id, weight)
SELECT i.id, d.weight
FROM (VALUES
    ('GLOCK_WATER_ELEMENTAL', 45),
    ('USP_GUARDIAN', 30),
    ('AK_REDLINE', 15),
    ('AWP_ELECTRIC_HIVE', 8),
    ('AWP_ASIIMOV', 2)
) AS d(item_code, weight)
JOIN items i ON i.code = d.item_code;