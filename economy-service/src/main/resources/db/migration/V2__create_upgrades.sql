CREATE TABLE upgrades (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(64)    NOT NULL,
    name         VARCHAR(100)   NOT NULL,
    description  VARCHAR(500)   NOT NULL,
    tier         INT            NOT NULL,
    price        NUMERIC(19, 2) NOT NULL,
    click_power  NUMERIC(19, 2) NOT NULL,
    active       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_upgrades_code UNIQUE (code),
    CONSTRAINT ck_upgrades_tier CHECK (tier >= 1),
    CONSTRAINT ck_upgrades_price CHECK (price >= 0),
    CONSTRAINT ck_upgrades_click_power CHECK (click_power > 0)
);

CREATE INDEX idx_upgrades_active_tier ON upgrades (active, tier);

INSERT INTO upgrades (code, name, description, tier, price, click_power) VALUES
('BETTER_MOUSE', 'Better Mouse',
'A slightly better mouse. +1 ClickDollar per click.', 1, 50.00, 1.00),
('GAMING_MOUSE', 'Gaming Mouse',
'Precision gaming mouse. +2 ClickDollars per click.', 2, 200.00, 2.00),
('MECHANICAL_KEYBOARD', 'Mechanical Keyboard',
'Satisfying clicks, more dollars. +4 per click.', 3, 500.00, 4.00),
('GAMING_SETUP', 'Gaming Setup',
'Desk, chair, dual monitors. +10 per click.', 4, 2000.00, 10.00),
('AI_AUTO_CLICKER', 'AI Auto Clicker',
'Lets AI help you click. +25 per click.', 5, 10000.00, 25.00);