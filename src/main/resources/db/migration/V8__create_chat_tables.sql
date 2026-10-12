CREATE TABLE chats (
    lower_user_id UUID NOT NULL,
    higher_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    visible_since_lower TIMESTAMPTZ,
    visible_since_higher TIMESTAMPTZ,
    PRIMARY KEY (lower_user_id, higher_user_id),
    CONSTRAINT fk_chat_lower_user FOREIGN KEY (lower_user_id) REFERENCES users(user_id),
    CONSTRAINT fk_chat_higher_user FOREIGN KEY (higher_user_id) REFERENCES users(user_id),
    CONSTRAINT ck_chat_distinct_users CHECK (lower_user_id <> higher_user_id)
);

CREATE INDEX idx_chats_higher_user ON chats (higher_user_id);

CREATE TABLE messages (
    message_id UUID PRIMARY KEY,
    lower_user_id UUID NOT NULL,
    higher_user_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMPTZ,
    CONSTRAINT fk_message_chat FOREIGN KEY (lower_user_id, higher_user_id) REFERENCES chats(lower_user_id, higher_user_id) ON DELETE CASCADE,
    CONSTRAINT ck_message_sender_in_chat CHECK (sender_id IN (lower_user_id, higher_user_id))
);

CREATE INDEX idx_messages_chat_created ON messages (lower_user_id, higher_user_id, created_at DESC);

CREATE INDEX idx_messages_unread ON messages (lower_user_id, higher_user_id) WHERE read_at IS NULL;