CREATE TABLE user_inventory_items (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    item_id      BIGINT       NOT NULL,
    exterior     VARCHAR(32)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_inventory_items_item
        FOREIGN KEY (item_id) REFERENCES items (id),
    CONSTRAINT ck_user_inventory_exterior CHECK (
        exterior IN (
            'FACTORY_NEW',
            'MINIMAL_WEAR',
            'FIELD_TESTED',
            'WELL_WORN',
            'BATTLE_SCARRED'
        )
    )
);

CREATE INDEX idx_user_inventory_items_user_id ON user_inventory_items (user_id);