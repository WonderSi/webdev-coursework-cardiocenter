-- ==========================================
-- Токены
-- ==========================================

CREATE TABLE refresh_tokens (
    id            BIGSERIAL    PRIMARY KEY,
    creator_id    BIGINT       NOT NULL REFERENCES admin_users (id) ON DELETE CASCADE,
    token_hash    TEXT         NOT NULL UNIQUE,
    expires_date  TIMESTAMPTZ  NOT NULL,
    created_date  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_date  TIMESTAMPTZ  NULL

);

-- Индекс для поиска сессий конкретного сотрудника и удаления связанных токенов при удалении его учетной записи
CREATE INDEX idx_refresh_tokens_creator_id ON refresh_tokens (creator_id);