CREATE TABLE recent_drops (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    item_code         VARCHAR(64)  NOT NULL,
    item_display_name VARCHAR(128) NOT NULL,
    rarity            VARCHAR(32)  NOT NULL,
    exterior          VARCHAR(32)  NOT NULL,
    stat_track        BOOLEAN      NOT NULL DEFAULT FALSE,
    source            VARCHAR(16)  NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_recent_drops_created_at ON recent_drops (created_at DESC);