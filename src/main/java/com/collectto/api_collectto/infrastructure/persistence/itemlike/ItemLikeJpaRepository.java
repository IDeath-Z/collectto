package com.collectto.api_collectto.infrastructure.persistence.itemlike;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemLikeJpaRepository extends JpaRepository<ItemLikeJpaEntity, ItemLikeJpaId> {

    @Query("SELECT il FROM ItemLikeJpaEntity il WHERE il.id.itemId = :itemId")
    Page<ItemLikeJpaEntity> findByItemId(@Param("itemId") UUID itemId, Pageable pageRequest);

    @Query("SELECT il FROM ItemLikeJpaEntity il WHERE il.id.likerId = :likerId")
    Page<ItemLikeJpaEntity> findByLikerId(@Param("likerId") UUID likerId, Pageable pageRequest);

    @Query("SELECT il.id.itemId FROM ItemLikeJpaEntity il WHERE il.id.likerId = :likerId AND il.id.itemId IN :itemIds")
    Set<UUID> findLikedItemIds(@Param("likerId") UUID likerId, @Param("itemIds") List<UUID> itemIds);
}
