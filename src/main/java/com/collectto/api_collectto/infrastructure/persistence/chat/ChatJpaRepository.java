package com.collectto.api_collectto.infrastructure.persistence.chat;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatJpaRepository extends JpaRepository<ChatJpaEntity, ChatJpaId> {

    @Query("""
    SELECT c FROM ChatJpaEntity c
    WHERE (c.id.lowerUserId = :userId AND c.visibleSinceLower IS NOT NULL)
       OR (c.id.higherUserId = :userId AND c.visibleSinceHigher IS NOT NULL)
    """)
    Slice<ChatJpaEntity> findAllVisibleByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query(value = """
        SELECT CASE WHEN lower_user_id = :userId THEN higher_user_id ELSE lower_user_id END
        FROM chats
        WHERE lower_user_id = :userId OR higher_user_id = :userId
        """, nativeQuery = true)
    List<UUID> findPartnerIds(@Param("userId") UUID userId);
}
