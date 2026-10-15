CREATE TABLE password_reset_tokens (
    id               BIGSERIAL PRIMARY KEY,
    token            VARCHAR(64)  NOT NULL,
    user_id          BIGINT       NOT NULL,
    expiration_date  TIMESTAMPTZ  NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    used_at          TIMESTAMPTZ  NULL,
    CONSTRAINT uk_password_reset_token UNIQUE (token),
    CONSTRAINT fk_password_reset_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_user_id
    ON password_reset_tokens (user_id);