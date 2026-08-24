CREATE TABLE email_verification_tokens (
        id               BIGSERIAL PRIMARY KEY,
        token            VARCHAR(64)  NOT NULL,
        user_id          BIGINT       NOT NULL,
        expiration_date  TIMESTAMPTZ  NOT NULL,
        created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
        CONSTRAINT uk_email_verification_token UNIQUE (token),
        CONSTRAINT fk_email_verification_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_email_verification_user_id
    ON email_verification_tokens (user_id);