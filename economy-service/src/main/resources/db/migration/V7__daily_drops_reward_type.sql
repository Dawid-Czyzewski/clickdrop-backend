ALTER TABLE daily_drops
    ALTER COLUMN item_id DROP NOT NULL;

ALTER TABLE daily_drops
DROP CONSTRAINT uk_daily_drops_item;

ALTER TABLE daily_drops
    ADD COLUMN reward_type VARCHAR(16) NOT NULL DEFAULT 'SKIN',
    ADD COLUMN cash_amount NUMERIC(19, 2);

ALTER TABLE daily_drops
    ALTER COLUMN reward_type DROP DEFAULT;

ALTER TABLE daily_drops
    ADD CONSTRAINT ck_daily_drops_reward CHECK (
        (reward_type = 'SKIN'
            AND item_id IS NOT NULL
            AND cash_amount IS NULL)
            OR
        (reward_type = 'CASH'
            AND item_id IS NULL
            AND cash_amount IS NOT NULL
            AND cash_amount > 0)
        );

CREATE UNIQUE INDEX uk_daily_drops_skin_item
    ON daily_drops (item_id)
    WHERE reward_type = 'SKIN';

INSERT INTO daily_drops (reward_type, item_id, cash_amount, weight)
VALUES
    ('CASH', NULL, 15.00, 35),
    ('CASH', NULL, 40.00, 10);