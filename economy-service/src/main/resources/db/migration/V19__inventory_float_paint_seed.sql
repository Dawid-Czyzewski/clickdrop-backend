ALTER TABLE user_inventory_items
    ADD COLUMN float_value NUMERIC(8, 6),
    ADD COLUMN paint_seed INT;

UPDATE user_inventory_items
SET float_value = CASE exterior
        WHEN 'FACTORY_NEW' THEN 0.035000
        WHEN 'MINIMAL_WEAR' THEN 0.110000
        WHEN 'FIELD_TESTED' THEN 0.265000
        WHEN 'WELL_WORN' THEN 0.415000
        WHEN 'BATTLE_SCARRED' THEN 0.725000
        ELSE 0.250000
    END,
    paint_seed = ((id * 37) % 1000)
WHERE float_value IS NULL OR paint_seed IS NULL;

ALTER TABLE user_inventory_items
    ALTER COLUMN float_value SET NOT NULL,
    ALTER COLUMN paint_seed SET NOT NULL;

ALTER TABLE user_inventory_items
    ADD CONSTRAINT ck_user_inventory_float_value
        CHECK (float_value >= 0 AND float_value < 1),
    ADD CONSTRAINT ck_user_inventory_paint_seed
        CHECK (paint_seed >= 0 AND paint_seed <= 999);

ALTER TABLE battle_round_drops
    ADD COLUMN float_value NUMERIC(8, 6),
    ADD COLUMN paint_seed INT;
