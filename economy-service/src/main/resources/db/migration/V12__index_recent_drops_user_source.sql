CREATE INDEX idx_recent_drops_user_source_created_at
    ON recent_drops (user_id, source, created_at DESC);