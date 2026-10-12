package com.collectto.api_collectto.infrastructure.persistence.message;

import java.time.Instant;
import java.util.List;
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
}