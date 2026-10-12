package com.collectto.api_collectto.domain.entities;

import java.time.Instant;
import java.util.UUID;

import com.collectto.api_collectto.domain.shared.DomainValidator;

public final class Message {

    private final UUID id;
    private final UUID lowerUserId;
    private final UUID higherUserId;
    private final UUID senderId;
    private final String content;
    private final Instant createdAt;
    private final Instant readAt;

    public Message(UUID id, UUID lowerUserId, UUID higherUserId, UUID senderId, String content, Instant createdAt, Instant readAt) {
        this.id = DomainValidator.requireNonNull(id, "Message ID cannot be null");
        this.lowerUserId = DomainValidator.requireNonNull(lowerUserId, "Lower user ID cannot be null");
        this.higherUserId = DomainValidator.requireNonNull(higherUserId, "Higher user ID cannot be null");
        this.senderId = DomainValidator.requireNonNull(senderId, "Sender ID cannot be null");
        this.content = DomainValidator.requireNonNull(content, "Message content cannot be null");
        this.createdAt = DomainValidator.requireNonNull(createdAt, "Creation timestamp cannot be null");
        this.readAt = readAt;
    }

    public static Message createMessage(Chat chat, UUID senderId, String content) {

        return new Message(
            UUID.randomUUID(),
            chat.getLowerUserId(),
            chat.getHigherUserId(),
            senderId,
            content,
            Instant.now(),
            null
        );
    }

    public Message markAsRead() {
        return new Message(
            this.id,
            this.lowerUserId,
            this.higherUserId,
            this.senderId,
            this.content,
            this.createdAt,
            Instant.now()
        );
    }

    public UUID otherParticipantFor(UUID userId) {
        DomainValidator.requireNonNull(userId, "User ID cannot be null");

        if (userId.equals(this.lowerUserId))
            return this.higherUserId;

        if (userId.equals(this.higherUserId)) 
            return this.lowerUserId;
        
        throw new IllegalArgumentException("User does not belong to this message's chat");
    }

    public UUID getId() {
        return id;
    }

    public UUID getLowerUserId() {
        return lowerUserId;
    }

    public UUID getHigherUserId() {
        return higherUserId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getReadAt() {
        return readAt;
    }
}
