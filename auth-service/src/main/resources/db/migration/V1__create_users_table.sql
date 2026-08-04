CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    username    VARCHAR(50)     NOT NULL,
    role        VARCHAR(32)     NOT NULL,
    experience  INTEGER         NOT NULL DEFAULT 0,
    level       INTEGER         NOT NULL DEFAULT 1,
    dollars     NUMERIC(19, 2)  NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_username UNIQUE (username)
);