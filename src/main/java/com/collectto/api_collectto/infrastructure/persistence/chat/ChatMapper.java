package com.collectto.api_collectto.infrastructure.persistence.chat;

import org.springframework.stereotype.Component;

import com.collectto.api_collectto.domain.entities.Chat;

@Component 
public final class ChatMapper {

    public ChatJpaEntity toJpa(Chat chat) {
        ChatJpaId id = new ChatJpaId(chat.getLowerUserId(), chat.getHigherUserId());

        ChatJpaEntity entity = new ChatJpaEntity();
        entity.setId(id);
        entity.setCreatedAt(chat.getCreatedAt());
        entity.setVisibleSinceLower(chat.getVisibleSinceLower());
        entity.setVisibleSinceHigher(chat.getVisibleSinceHigher());
        return entity;
    }

    public Chat toDomain(ChatJpaEntity entity) {
        return new Chat(
                entity.getId().getLowerUserId(),
                entity.getId().getHigherUserId(),
                entity.getCreatedAt(),
                entity.getVisibleSinceLower(),
                entity.getVisibleSinceHigher()
        );
    }
}
