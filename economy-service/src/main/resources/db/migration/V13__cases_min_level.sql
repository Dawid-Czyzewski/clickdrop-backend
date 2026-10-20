ALTER TABLE cases
    ADD COLUMN IF NOT EXISTS min_level INT NOT NULL DEFAULT 1;

ALTER TABLE cases
    DROP CONSTRAINT IF EXISTS ck_cases_min_level;

ALTER TABLE cases
    ADD CONSTRAINT ck_cases_min_level CHECK (min_level >= 1);

UPDATE cases SET min_level = 1  WHERE price <= 75;
UPDATE cases SET min_level = 3  WHERE price > 75 AND price <= 88;
UPDATE cases SET min_level = 5  WHERE price > 88 AND price <= 100;
UPDATE cases SET min_level = 8  WHERE price > 100 AND price <= 110;
UPDATE cases SET min_level = 10 WHERE price > 110;
