package com.collectto.api_collectto.domain.ports;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.collectto.api_collectto.domain.entities.Message;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;
import com.collectto.api_collectto.domain.shared.DomainSlice;

public interface MessageRepository {

    DomainSlice<Message> findVisibleByUsersIds(UUID lowerUserId, UUID higherUserId, Instant visibleSince, DomainPageRequest pageRequest);
    Message save(Message message);
    List<Message> findLastMessagesByOtherUserIds(UUID requesterId, List<UUID> otherUserIds);
}
