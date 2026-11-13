CREATE TABLE battles (
    id                   BIGSERIAL PRIMARY KEY,
    case_id              BIGINT NOT NULL,
    rounds               INT NOT NULL,
    status               VARCHAR(32) NOT NULL,
    player1_user_id      BIGINT NOT NULL,
    player2_user_id      BIGINT,
    player1_total_value  NUMERIC(19, 2) NOT NULL DEFAULT 0,
    player2_total_value  NUMERIC(19, 2) NOT NULL DEFAULT 0,
    winner_user_id       BIGINT,
    entry_cost           NUMERIC(19, 2) NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL,
    finished_at          TIMESTAMPTZ,
    CONSTRAINT fk_battles_case FOREIGN KEY (case_id) REFERENCES cases (id),
    CONSTRAINT chk_battles_rounds CHECK (rounds IN (1, 3, 5)),
    CONSTRAINT chk_battles_status CHECK (status IN ('WAITING', 'FINISHED'))
);

CREATE TABLE battle_round_drops (
    id                 BIGSERIAL PRIMARY KEY,
    battle_id          BIGINT NOT NULL,
    round_index        INT NOT NULL,
    side               VARCHAR(16) NOT NULL,
    item_id            BIGINT NOT NULL,
    exterior           VARCHAR(32) NOT NULL,
    stat_track         BOOLEAN NOT NULL DEFAULT FALSE,
    drop_value         NUMERIC(19, 2) NOT NULL,
    inventory_item_id  BIGINT,
    CONSTRAINT fk_battle_round_drops_battle FOREIGN KEY (battle_id) REFERENCES battles (id),
    CONSTRAINT fk_battle_round_drops_item FOREIGN KEY (item_id) REFERENCES items (id),
    CONSTRAINT chk_battle_round_drops_side CHECK (side IN ('PLAYER1', 'PLAYER2'))
);

CREATE INDEX idx_battles_player1 ON battles (player1_user_id);
CREATE INDEX idx_battles_status ON battles (status);
CREATE INDEX idx_battle_round_drops_battle ON battle_round_drops (battle_id);
