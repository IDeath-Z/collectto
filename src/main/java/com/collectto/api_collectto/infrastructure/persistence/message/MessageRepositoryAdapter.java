package com.collectto.api_collectto.infrastructure.persistence.message;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.collectto.api_collectto.domain.entities.Message;
import com.collectto.api_collectto.domain.ports.MessageRepository;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;
import com.collectto.api_collectto.domain.shared.DomainSlice;
import com.collectto.api_collectto.infrastructure.persistence.shared.PageConverter;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MessageRepositoryAdapter implements MessageRepository {

    private final MessageJpaRepository messageJpaRepository;
    private final MessageMapper messageMapper;

    @Override
    public DomainSlice<Message> findVisibleByUsersIds(UUID lowerUserId, UUID higherUserId, Instant visibleSince, DomainPageRequest pageRequest) {
        PageRequest springPage = PageConverter.toSpring(pageRequest);

        Slice<Message> slice = messageJpaRepository
            .findVisibleByUsersIds(lowerUserId, higherUserId, visibleSince, springPage)
            .map(messageMapper::toDomain);

        return new DomainSlice<>(slice.getContent(), slice.hasNext());
    }

    @Override
    public Message save(Message message) {
        return messageMapper.toDomain(messageJpaRepository.save(messageMapper.toJpa(message)));
    }

    @Override
    public List<Message> findLastMessagesByOtherUserIds(UUID requesterId, List<UUID> otherUserIds) {
        return messageJpaRepository.findLastMessages(requesterId, otherUserIds).stream()
            .map(messageMapper::toDomain)
            .toList();
    }

    @Override
    public int markAllAsReadFromSender(UUID lowerUserId, UUID higherUserId, UUID senderId, Instant readAt) {
        return messageJpaRepository.markAllAsReadFromSender(lowerUserId, higherUserId, senderId, readAt);
    }

    @Override
    public UnreadTotals countUnreadTotals(UUID userId) {
        List<Object[]> rows = messageJpaRepository.countUnreadTotals(userId);
        if (rows.isEmpty())
            return new UnreadTotals(0, 0);
        Object[] row = rows.get(0);
        return new UnreadTotals(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
    }

    @Override
    public Map<UUID, Long> countUnreadByOtherUserIds(UUID requesterId, List<UUID> otherUserIds) {
        if (otherUserIds.isEmpty())
            return Map.of();
        Map<UUID, Long> unreadByOtherUserId = new HashMap<>();
        for (Object[] row : messageJpaRepository.countUnreadByOtherUserIds(requesterId, otherUserIds))
            unreadByOtherUserId.put((UUID) row[0], ((Number) row[1]).longValue());
        return unreadByOtherUserId;
    }
}
