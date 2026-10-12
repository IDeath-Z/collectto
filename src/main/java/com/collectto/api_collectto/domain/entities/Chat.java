package com.collectto.api_collectto.domain.entities;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.collectto.api_collectto.domain.shared.DomainValidator;

public final class Chat {

    private final UUID lowerUserId;
    private final UUID higherUserId;
    private final Instant createdAt;
    private final Instant visibleSinceLower;
    private final Instant visibleSinceHigher;

    // Used only for comparisons to know if a uuid is lower or higher, just for convenience
    public record ChatId(UUID firstUserId, UUID secondUserId) {

        public ChatId {
            DomainValidator.requireNonNull(firstUserId, "First user ID cannot be null");
            DomainValidator.requireNonNull(secondUserId, "Second user ID cannot be null");

            if (firstUserId.equals(secondUserId)) {
                throw new IllegalArgumentException("A user cannot chat with themselves");
            }
        }

        public UUID getLowerUserId() {
            return firstUserId.compareTo(secondUserId) < 0 ? firstUserId : secondUserId;
        }

        public UUID getHigherUserId() {
            return firstUserId.compareTo(secondUserId) < 0 ? secondUserId : firstUserId;
        }
    }

    public Chat(UUID lowerUserId, UUID higherUserId, Instant createdAt, Instant visibleSinceLower, Instant visibleSinceHigher) {
        this.lowerUserId = DomainValidator.requireNonNull(lowerUserId, "Lower user ID cannot be null");
        this.higherUserId = DomainValidator.requireNonNull(higherUserId, "Higher user ID cannot be null");
        this.createdAt = DomainValidator.requireNonNull(createdAt, "Creation timestamp cannot be null");
        this.visibleSinceLower = visibleSinceLower;
        this.visibleSinceHigher = visibleSinceHigher;
    }

    public static Chat createNewChat(ChatId chatId) {
        Instant now = Instant.now();
        return new Chat(
            chatId.getLowerUserId(),
            chatId.getHigherUserId(),
            now,
            now,
            now
        );
    }

    public Chat clearHistoryFor(UUID userId) {
        if (userId.equals(this.lowerUserId)) {
            return new Chat(
                this.lowerUserId,
                this.higherUserId,
                this.createdAt,
                null,
                this.visibleSinceHigher
            );
        } else if (userId.equals(this.higherUserId)) {
            return new Chat(
                this.lowerUserId,
                this.higherUserId,
                this.createdAt,
                this.visibleSinceLower,
                null
            );
        } else {
            throw new IllegalArgumentException("User does not belong to this chat");
        }
    }

    public Optional<Instant> visibleSinceFor(UUID userId) {
    DomainValidator.requireNonNull(userId, "User ID cannot be null");
        
        if (userId.equals(this.lowerUserId))
            return Optional.ofNullable(this.visibleSinceLower);

        if (userId.equals(this.higherUserId))
            return Optional.ofNullable(this.visibleSinceHigher);

        throw new IllegalArgumentException("User does not belong to this chat");
    }

    public Chat makeVisibleFrom(Instant from) {
        DomainValidator.requireNonNull(from, "Visibility timestamp cannot be null");

        return new Chat(
            this.lowerUserId,
            this.higherUserId,
            this.createdAt,
            this.visibleSinceLower != null ? this.visibleSinceLower : from,
            this.visibleSinceHigher != null ? this.visibleSinceHigher : from
        );
    }

    public UUID otherUserIdFor(UUID userId) {
        DomainValidator.requireNonNull(userId, "User ID cannot be null");
        
        if (userId.equals(this.lowerUserId))
            return this.higherUserId;
        
        if (userId.equals(this.higherUserId))
            return this.lowerUserId;

        throw new IllegalArgumentException("User does not belong to this chat");
    }

    public UUID getLowerUserId() {
        return lowerUserId;
    }

    public UUID getHigherUserId() {
        return higherUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getVisibleSinceLower() {
        return visibleSinceLower;
    }

    public Instant getVisibleSinceHigher() {
        return visibleSinceHigher;
    }
}
