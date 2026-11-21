package com.collectto.api_collectto.domain.ports;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.collectto.api_collectto.domain.entities.Message;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;
import com.collectto.api_collectto.domain.shared.DomainSlice;

public interface MessageRepository {

    DomainSlice<Message> findVisibleByUsersIds(UUID lowerUserId, UUID higherUserId, Instant visibleSince, DomainPageRequest pageRequest);
    Message save(Message message);

    record UnreadTotals(long chats, long messages) {}

    // Unread = sent by the other participant, readAt null, and inside the window the user can still see (after a clear).
    UnreadTotals countUnreadTotals(UUID userId);

    // Unread messages per chat, keyed by the other participant's id. Chats with nothing unread are absent.
    Map<UUID, Long> countUnreadByOtherUserIds(UUID requesterId, List<UUID> otherUserIds);

    // Sets readAt on every unread message sent by senderId in the chat. Returns how many were updated.
    int markAllAsReadFromSender(UUID lowerUserId, UUID higherUserId, UUID senderId, Instant readAt);
    List<Message> findLastMessagesByOtherUserIds(UUID requesterId, List<UUID> otherUserIds);
}
