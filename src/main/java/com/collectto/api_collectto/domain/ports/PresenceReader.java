package com.collectto.api_collectto.domain.ports;

import java.util.UUID;

// Ephemeral state of who is connected and who is typing to whom. Never persisted.
public interface PresenceReader {

    boolean isOnline(UUID userId);

    // true while typerId is typing a message to targetId
    boolean isTyping(UUID typerId, UUID targetId);
}
