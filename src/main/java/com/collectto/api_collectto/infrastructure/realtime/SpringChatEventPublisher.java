package com.collectto.api_collectto.infrastructure.realtime;

import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.collectto.api_collectto.domain.entities.Message;
import com.collectto.api_collectto.domain.ports.ChatEventPublisher;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpringChatEventPublisher implements ChatEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void messageCreated(Message message, UUID recipientId) {
        applicationEventPublisher.publishEvent(new ChatRealtimeEvents.MessageCreated(
            message.getId(), message.getSenderId(), recipientId, message.getContent(), message.getCreatedAt()));
    }

    @Override
    public void messagesRead(UUID readerId, UUID senderId, Instant readAt) {
        applicationEventPublisher.publishEvent(new ChatRealtimeEvents.MessagesRead(readerId, senderId, readAt));
    }
}
