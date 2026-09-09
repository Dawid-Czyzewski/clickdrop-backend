CREATE TABLE items (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(64)  NOT NULL,
    rarity       VARCHAR(32)  NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_items_code UNIQUE (code),
    CONSTRAINT ck_items_rarity CHECK (
        rarity IN ('MIL_SPEC', 'RESTRICTED', 'CLASSIFIED', 'COVERT')
    )
);

CREATE TABLE cases (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(64)    NOT NULL,
    price        NUMERIC(19, 2) NOT NULL,
    sort_order   INT            NOT NULL DEFAULT 0,
    active       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_cases_code UNIQUE (code),
    CONSTRAINT ck_cases_price CHECK (price > 0),
    CONSTRAINT ck_cases_sort_order CHECK (sort_order >= 0)
);

CREATE TABLE case_drops (
    id           BIGSERIAL PRIMARY KEY,
    case_id      BIGINT         NOT NULL,
    item_id      BIGINT         NOT NULL,
    weight       INT            NOT NULL,
    CONSTRAINT fk_case_drops_case FOREIGN KEY (case_id) REFERENCES cases (id),
    CONSTRAINT fk_case_drops_item FOREIGN KEY (item_id) REFERENCES items (id),
    CONSTRAINT uk_case_drops_case_item UNIQUE (case_id, item_id),
    CONSTRAINT ck_case_drops_weight CHECK (weight > 0)
);

CREATE INDEX idx_case_drops_case_id ON case_drops (case_id);
CREATE INDEX idx_items_active ON items (active);
CREATE INDEX idx_cases_active_sort ON cases (active, sort_order);

INSERT INTO items (code, rarity) VALUES
    ('GLOCK_WATER_ELEMENTAL', 'MIL_SPEC'),
    ('USP_GUARDIAN', 'MIL_SPEC'),
    ('AK_REDLINE', 'RESTRICTED'),
    ('AWP_ELECTRIC_HIVE', 'RESTRICTED'),
    ('M4A4_DESERT_STRIKE', 'CLASSIFIED'),
    ('AWP_ASIIMOV', 'COVERT');

INSERT INTO cases (code, price, sort_order) VALUES
    ('ANVIL', 50.00, 1),
    ('TEMPER', 99.00, 2),
    ('FORGEFIRE', 149.00, 3),
    ('EMBER', 200.00, 4),
    ('NOVA', 75.00, 5),
    ('RIFT', 350.00, 6),
    ('PULSE', 125.00, 7),
    ('SHADOW', 400.00, 8);

INSERT INTO case_drops (case_id, item_id, weight)
SELECT c.id, i.id, d.weight
FROM cases c
CROSS JOIN (VALUES
    ('GLOCK_WATER_ELEMENTAL', 50),
    ('USP_GUARDIAN', 30),
    ('AK_REDLINE', 15),
    ('AWP_ASIIMOV', 5)
) AS d(item_code, weight)
         JOIN items i ON i.code = d.item_code
WHERE c.code = 'ANVIL';

INSERT INTO case_drops (case_id, item_id, weight)
SELECT c.id, i.id, d.weight
FROM cases c
CROSS JOIN (VALUES
    ('USP_GUARDIAN', 40),
    ('AK_REDLINE', 35),
    ('AWP_ELECTRIC_HIVE', 20),
    ('M4A4_DESERT_STRIKE', 5)
) AS d(item_code, weight)
         JOIN items i ON i.code = d.item_code
WHERE c.code = 'TEMPER';