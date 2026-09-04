CREATE TABLE wallet (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT         NOT NULL,
    balance      NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_wallets_user_id UNIQUE (user_id)
);