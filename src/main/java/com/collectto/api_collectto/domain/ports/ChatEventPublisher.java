package com.collectto.api_collectto.domain.ports;

import java.time.Instant;
import java.util.UUID;

import com.collectto.api_collectto.domain.entities.Message;

// Announces chat facts to whoever cares (real-time delivery, for now).
// Implementations must only deliver after the surrounding transaction commits.
public interface ChatEventPublisher {

    void messageCreated(Message message, UUID recipientId);

    void messagesRead(UUID readerId, UUID senderId, Instant readAt);
}
