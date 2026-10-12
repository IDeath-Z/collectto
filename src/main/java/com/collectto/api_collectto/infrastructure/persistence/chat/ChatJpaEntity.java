package com.collectto.api_collectto.infrastructure.persistence.chat;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "chats")
public class ChatJpaEntity {

    @EmbeddedId
    private ChatJpaId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "visible_since_lower", nullable = true)
    private Instant visibleSinceLower;

    @Column(name = "visible_since_higher", nullable = true)
    private Instant visibleSinceHigher;
}
