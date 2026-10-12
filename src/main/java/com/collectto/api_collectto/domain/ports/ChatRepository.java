package com.collectto.api_collectto.domain.ports;

import java.util.Optional;
import java.util.UUID;

import com.collectto.api_collectto.domain.entities.Chat;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;
import com.collectto.api_collectto.domain.shared.DomainSlice;

public interface ChatRepository {

    Optional<Chat> findById(UUID lowerUserId, UUID higherUserId);
    DomainSlice<Chat> findAllVisibleByUserId(UUID userId, DomainPageRequest pageRequest);
    Chat save(Chat chat);
    boolean existsById(UUID lowerUserId, UUID higherUserId);
}
