package com.collectto.api_collectto.infrastructure.realtime;

import java.time.Instant;
import java.util.UUID;

// Spring application events raised inside the use case transaction and delivered only after commit.
public final class ChatRealtimeEvents {

    private ChatRealtimeEvents() {}

    public record MessageCreated(UUID messageId, UUID senderId, UUID recipientId, String content, Instant createdAt) {}

    public record MessagesRead(UUID readerId, UUID senderId, Instant readAt) {}
}
