package com.collectto.api_collectto.infrastructure.persistence.item;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.collectto.api_collectto.domain.enums.Visibility;

public interface ItemJpaRepository extends JpaRepository<ItemJpaEntity, UUID> {
    
    Optional<ItemJpaEntity> findByIdAndIsActiveTrue(UUID id);
    Page<ItemJpaEntity> findByCollectionIdAndIsActiveTrue(UUID collectionId, Pageable pageable);
    List<ItemJpaEntity> findByUserIdAndIsActiveTrue(UUID userId);
    List<ItemJpaEntity> findByCollectionIdAndNameContainingIgnoreCaseAndIsActiveTrue(UUID collectionId, String name);

    @Query("""
        SELECT i FROM ItemJpaEntity i 
        WHERE i.isActive = true 
        AND i.collection.isActive = true 
        AND i.collection.visibility = :publicVisibility 
        AND (i.name ILIKE %:query% OR i.description ILIKE %:query%)
    """)
    Page<ItemJpaEntity> searchPublicItems(
        @Param("query") String query, 
        @Param("publicVisibility") Visibility publicVisibility, 
        Pageable pageable
    );

    @Query(value = """
    SELECT collection_id AS "collectionId", ARRAY[media_url] AS "mediaUrls" FROM (
        SELECT 
            i.collection_id,
            i.media_urls[1] AS media_url,
            ROW_NUMBER() OVER(PARTITION BY i.collection_id ORDER BY i.created_at DESC) as rn
        FROM items i
        WHERE i.collection_id IN :collectionIds
          AND i.is_active = true
          AND i.media_urls IS NOT NULL
          AND array_length(i.media_urls, 1) > 0
    ) AS ranked_items
    WHERE rn <= 3
    """, nativeQuery = true)
    List<CollectionMediaProjection> findTop3MediaUrlsByCollectionIds(@Param("collectionIds") List<UUID> collectionIds);

    @Query("""
        SELECT DISTINCT i FROM ItemJpaEntity i 
        JOIN FETCH i.user u
        JOIN FETCH i.collection c
        JOIN FETCH i.tags t
        WHERE t.id IN :tagIds
        AND i.isActive = true
        AND c.isActive = true
        AND c.visibility = 'PUBLIC'
        AND i.mediaUrls IS NOT NULL
        AND u.id != :requesterId
        AND c.id NOT IN :excludedCollectionIds
    """)
    List<ItemJpaEntity> findRecommendedByTags(
        @Param("tagIds") Set<UUID> tagIds,
        @Param("requesterId") UUID requesterId,
        @Param("excludedCollectionIds") Set<UUID> excludedCollectionIds,
        Pageable pageable
    );

    @Query("""
        SELECT DISTINCT i FROM ItemJpaEntity i 
        JOIN FETCH i.user u
        JOIN FETCH i.collection c
        LEFT JOIN FETCH i.tags t
        WHERE i.isActive = true 
        AND c.isActive = true
        AND c.visibility = 'PUBLIC'
        AND i.mediaUrls IS NOT NULL
        AND u.id != :requesterId
        AND c.id NOT IN :excludedCollectionIds
        ORDER BY i.likesCount DESC
    """)
    List<ItemJpaEntity> findTrendingItems(
        @Param("requesterId") UUID requesterId,
        @Param("excludedCollectionIds") Set<UUID> excludedCollectionIds,
        Pageable pageable
    );

    @Query("""
        SELECT DISTINCT i FROM ItemJpaEntity i 
        JOIN FETCH i.user u
        JOIN FETCH i.collection c
        LEFT JOIN FETCH i.tags t
        WHERE i.isActive = true 
        AND c.isActive = true
        AND c.visibility = 'PUBLIC'
        AND i.mediaUrls IS NOT NULL
        AND u.id != :requesterId
        AND c.id NOT IN :excludedCollectionIds
        ORDER BY i.createdAt DESC
    """)
    List<ItemJpaEntity> findLatestItems(
        @Param("requesterId") UUID requesterId,
        @Param("excludedCollectionIds") Set<UUID> excludedCollectionIds,
        Pageable pageable
    );

    @Query("""
        SELECT DISTINCT i FROM ItemJpaEntity i 
        JOIN FETCH i.user u
        JOIN FETCH i.collection c
        LEFT JOIN FETCH i.tags t
        WHERE i.isActive = true 
        AND c.isActive = true
        AND (u.id IN :followedUsers OR c.id IN :followedCollections)
        AND (
            c.visibility = :publicVisibility 
            OR (c.visibility = :friendsVisibility AND u.id IN :friendUsers)
        )
        ORDER BY i.createdAt DESC
    """)
    List<ItemJpaEntity> findFeedItems(
        @Param("followedUsers") Set<UUID> followedUsers, 
        @Param("followedCollections") Set<UUID> followedCollections, 
        @Param("friendUsers") Set<UUID> friendUsers, 
        @Param("publicVisibility") Visibility publicVisibility,
        @Param("friendsVisibility") Visibility friendsVisibility,
        Pageable pageable
    );

    @Modifying @Query("UPDATE ItemJpaEntity i SET i.likesCount = i.likesCount + 1 WHERE i.id = :itemId")
    void incrementLikesCount(UUID itemId);

    @Modifying @Query("UPDATE ItemJpaEntity i SET i.likesCount = i.likesCount - 1 WHERE i.id = :itemId")
    void decrementLikesCount(UUID itemId);

    @Modifying @Query("UPDATE ItemJpaEntity i SET i.commentsCount = i.commentsCount + 1 WHERE i.id = :itemId")
    void incrementCommentsCount(UUID itemId);

    @Modifying @Query("UPDATE ItemJpaEntity i SET i.commentsCount = i.commentsCount - 1 WHERE i.id = :itemId")
    void decrementCommentsCount(UUID itemId);

    @Modifying @Query("UPDATE ItemJpaEntity i SET i.isActive = false, i.deactivatedAt = CURRENT_TIMESTAMP WHERE i.id = :itemId")
    void deactivateItem(@Param("itemId") UUID itemId);

    @Modifying @Query(value = """
        DELETE FROM items 
        WHERE is_active = false 
        AND deactivated_at <= :cutoffDate
    """, nativeQuery = true)
    int purgeOldDeactivatedItems(@Param("cutoffDate") Instant cutoffDate);

    @Query(value = """
        SELECT * FROM items 
        WHERE is_active = false 
        AND deactivated_at <= :cutoffDate
    """, nativeQuery = true)
    List<ItemJpaEntity> findItemsToPurge(@Param("cutoffDate") Instant cutoffDate);
}
