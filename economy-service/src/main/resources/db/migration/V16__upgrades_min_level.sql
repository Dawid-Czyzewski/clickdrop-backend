ALTER TABLE upgrades
    ADD COLUMN IF NOT EXISTS min_level INT NOT NULL DEFAULT 1;

ALTER TABLE upgrades
    DROP CONSTRAINT IF EXISTS ck_upgrades_min_level;

ALTER TABLE upgrades
    ADD CONSTRAINT ck_upgrades_min_level CHECK (min_level >= 1);

UPDATE upgrades SET min_level = CASE tier
    WHEN 1 THEN 1
    WHEN 2 THEN 5
    WHEN 3 THEN 8
    WHEN 4 THEN 12
    WHEN 5 THEN 15
    WHEN 6 THEN 18
    WHEN 7 THEN 20
    WHEN 8 THEN 22
    ELSE GREATEST(1, (tier - 1) * 3 + 2)
END;
