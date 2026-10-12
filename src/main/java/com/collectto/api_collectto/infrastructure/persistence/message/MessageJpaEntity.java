package com.collectto.api_collectto.infrastructure.persistence.message;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "messages")
public class MessageJpaEntity {

    @Id
    @Column(name = "message_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "lower_user_id", nullable = false, updatable = false)
    private UUID lowerUserId;

    @Column(name = "higher_user_id", nullable = false, updatable = false)
    private UUID higherUserId;

    @Column(name = "sender_id", nullable = false, updatable = false)
    private UUID senderId;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;
}