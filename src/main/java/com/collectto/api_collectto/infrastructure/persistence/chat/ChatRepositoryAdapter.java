package com.collectto.api_collectto.infrastructure.persistence.chat;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.collectto.api_collectto.domain.entities.Chat;
import com.collectto.api_collectto.domain.ports.ChatRepository;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;
import com.collectto.api_collectto.domain.shared.DomainSlice;
import com.collectto.api_collectto.infrastructure.persistence.shared.PageConverter;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public final class ChatRepositoryAdapter implements ChatRepository {

    private final ChatJpaRepository chatJpaRepository;
    private final ChatMapper chatMapper;

    @Override
    public Optional<Chat> findById(UUID lowerUserId, UUID higherUserId) {
        return chatJpaRepository.findById(new ChatJpaId(lowerUserId, higherUserId)).map(chatMapper::toDomain);
    }

    @Override
    public Chat save(Chat chat) {
        ChatJpaEntity entity = chatMapper.toJpa(chat);
        return chatMapper.toDomain(chatJpaRepository.save(entity));
    }

    @Override
    public boolean existsById(UUID lowerUserId, UUID higherUserId) {
        return chatJpaRepository.existsById(new ChatJpaId(lowerUserId, higherUserId));
    }

    @Override
    public DomainSlice<Chat> findAllVisibleByUserId(UUID userId, DomainPageRequest pageRequest) {
        PageRequest springPage = PageConverter.toSpring(pageRequest);
    
        Slice<Chat> slice = chatJpaRepository
            .findAllVisibleByUserId(userId, springPage)
            .map(chatMapper::toDomain);
    
        return new DomainSlice<>(slice.getContent(), slice.hasNext());
    }
}
