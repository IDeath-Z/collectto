package com.collectto.api_collectto.infrastructure.persistence.message;

import org.springframework.stereotype.Component;

import com.collectto.api_collectto.domain.entities.Message;

@Component 
public final class MessageMapper {

    public MessageJpaEntity toJpa(Message message) {
        MessageJpaEntity entity = new MessageJpaEntity();
        entity.setId(message.getId());
        entity.setLowerUserId(message.getLowerUserId());
        entity.setHigherUserId(message.getHigherUserId());
        entity.setSenderId(message.getSenderId());
        entity.setContent(message.getContent());
        entity.setCreatedAt(message.getCreatedAt());
        entity.setReadAt(message.getReadAt());
        return entity;
    }

    public Message toDomain(MessageJpaEntity entity) {
        return new Message(
                entity.getId(),
                entity.getLowerUserId(),
                entity.getHigherUserId(),
                entity.getSenderId(),
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getReadAt()
        );
    }
}
