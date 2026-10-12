package com.collectto.api_collectto.infrastructure.persistence.message;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageJpaRepository extends JpaRepository<MessageJpaEntity, UUID> {

    @Query("""
        SELECT m FROM MessageJpaEntity m
        WHERE m.lowerUserId = :lowerUserId
          AND m.higherUserId = :higherUserId
          AND m.createdAt >= :visibleSince
        """)
    Slice<MessageJpaEntity> findVisibleByUsersIds(
        @Param("lowerUserId") UUID lowerUserId,
        @Param("higherUserId") UUID higherUserId,
        @Param("visibleSince") Instant visibleSince,
        Pageable pageable
    );

    @Query(value = """
        SELECT DISTINCT ON (lower_user_id, higher_user_id) *
        FROM messages
        WHERE (lower_user_id = :requesterId AND higher_user_id IN (:otherUserIds))
           OR (higher_user_id = :requesterId AND lower_user_id IN (:otherUserIds))
        ORDER BY lower_user_id, higher_user_id, created_at DESC, message_id DESC
    """, nativeQuery = true)
    List<MessageJpaEntity> findLastMessages(
        @Param("requesterId") UUID requesterId,
        @Param("otherUserIds") List<UUID> otherUserIds
    );
}