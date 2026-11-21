package com.collectto.api_collectto.infrastructure.persistence.message;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying
    @Query("""
        UPDATE MessageJpaEntity m
        SET m.readAt = :readAt
        WHERE m.lowerUserId = :lowerUserId
          AND m.higherUserId = :higherUserId
          AND m.senderId = :senderId
          AND m.readAt IS NULL
        """)
    int markAllAsReadFromSender(
        @Param("lowerUserId") UUID lowerUserId,
        @Param("higherUserId") UUID higherUserId,
        @Param("senderId") UUID senderId,
        @Param("readAt") Instant readAt
    );

    @Query(value = """
        SELECT CASE WHEN m.lower_user_id = :requesterId THEN m.higher_user_id ELSE m.lower_user_id END AS other_user_id,
               COUNT(*) AS unread
        FROM messages m
        JOIN chats c ON c.lower_user_id = m.lower_user_id AND c.higher_user_id = m.higher_user_id
        WHERE (m.lower_user_id = :requesterId OR m.higher_user_id = :requesterId)
          AND m.sender_id IN (:otherUserIds)
          AND m.read_at IS NULL
          AND m.created_at >= CASE WHEN c.lower_user_id = :requesterId THEN c.visible_since_lower ELSE c.visible_since_higher END
        GROUP BY 1
        """, nativeQuery = true)
    List<Object[]> countUnreadByOtherUserIds(
        @Param("requesterId") UUID requesterId,
        @Param("otherUserIds") List<UUID> otherUserIds
    );

    @Query(value = """
        SELECT COUNT(DISTINCT m.sender_id) AS chats, COUNT(*) AS messages
        FROM messages m
        JOIN chats c ON c.lower_user_id = m.lower_user_id AND c.higher_user_id = m.higher_user_id
        WHERE (m.lower_user_id = :userId OR m.higher_user_id = :userId)
          AND m.sender_id <> :userId
          AND m.read_at IS NULL
          AND m.created_at >= CASE WHEN c.lower_user_id = :userId THEN c.visible_since_lower ELSE c.visible_since_higher END
        """, nativeQuery = true)
    List<Object[]> countUnreadTotals(@Param("userId") UUID userId);
}
