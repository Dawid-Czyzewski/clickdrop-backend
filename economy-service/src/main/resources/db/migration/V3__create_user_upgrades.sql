CREATE TABLE user_upgrades (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT         NOT NULL,
    upgrade_id  BIGINT         NOT NULL,
    level       INT            NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_user_upgrades_user_upgrade UNIQUE (user_id, upgrade_id),
    CONSTRAINT fk_user_upgrades_upgrade
        FOREIGN KEY (upgrade_id) REFERENCES upgrades (id),
    CONSTRAINT ck_user_upgrades_level CHECK (level >= 0)
);

CREATE INDEX idx_user_upgrades_user_id ON user_upgrades (user_id);