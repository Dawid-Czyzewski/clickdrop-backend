ALTER TABLE upgrades
    ADD COLUMN income_per_second NUMERIC(19, 4) NOT NULL DEFAULT 0;

ALTER TABLE upgrades
    DROP CONSTRAINT ck_upgrades_click_power;

ALTER TABLE upgrades
    ADD CONSTRAINT ck_upgrades_click_power CHECK (click_power >= 0);

ALTER TABLE upgrades
    ADD CONSTRAINT ck_upgrades_income_per_second CHECK (income_per_second >= 0);

ALTER TABLE wallet
    ADD COLUMN last_idle_collected_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

UPDATE wallet
SET last_idle_collected_at = COALESCE(updated_at, created_at, NOW());

INSERT INTO upgrades (code, name, description, tier, price, click_power, income_per_second) VALUES
('BOT_FARM', 'Bot Farm',
'Small bot farm. Earns ClickDollars while you are away.', 6, 750.00, 0.00, 0.1000),
('AFK_SCRIPT', 'AFK Script',
'Simple AFK script. Steady passive income.', 7, 2500.00, 0.00, 0.5000),
('SERVER_RACK', 'Server Rack',
'Rack of farm bots. Serious idle income.', 8, 12000.00, 0.00, 2.0000);