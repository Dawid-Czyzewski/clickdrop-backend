CREATE TABLE collections (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(128) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    reward_cd    NUMERIC(19, 2) NOT NULL,
    sort_order   INT NOT NULL DEFAULT 0,
    active       BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_collections_code UNIQUE (code)
);

CREATE TABLE collection_items (
    collection_id BIGINT NOT NULL,
    item_id       BIGINT NOT NULL,
    PRIMARY KEY (collection_id, item_id),
    CONSTRAINT fk_collection_items_collection
        FOREIGN KEY (collection_id) REFERENCES collections (id),
    CONSTRAINT fk_collection_items_item
        FOREIGN KEY (item_id) REFERENCES items (id)
);

CREATE TABLE user_collection_claims (
    user_id       BIGINT NOT NULL,
    collection_id BIGINT NOT NULL,
    claimed_at    TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, collection_id),
    CONSTRAINT fk_user_collection_claims_collection
        FOREIGN KEY (collection_id) REFERENCES collections (id)
);

CREATE INDEX idx_collection_items_item_id ON collection_items (item_id);
CREATE INDEX idx_user_collection_claims_collection ON user_collection_claims (collection_id);

-- MVP: 5 starter collections = early cases (distinct case_drops item_id)
INSERT INTO collections (code, display_name, reward_cd, sort_order, active)
SELECT
    c.code,
    c.display_name,
    (50 * COUNT(DISTINCT cd.item_id))::NUMERIC(19, 2),
    c.sort_order,
    TRUE
FROM cases c
JOIN case_drops cd ON cd.case_id = c.id
WHERE c.code IN (
    'CS20_CASE',
    'DANGER_ZONE_CASE',
    'SPECTRUM_2_CASE',
    'CLUTCH_CASE',
    'SNAKEBITE_CASE'
)
GROUP BY c.id, c.code, c.display_name, c.sort_order;

INSERT INTO collection_items (collection_id, item_id)
SELECT col.id, cd.item_id
FROM collections col
JOIN cases c ON c.code = col.code
JOIN case_drops cd ON cd.case_id = c.id
ON CONFLICT DO NOTHING;
