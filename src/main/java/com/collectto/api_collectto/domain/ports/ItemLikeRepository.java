package com.collectto.api_collectto.domain.ports;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.collectto.api_collectto.domain.entities.ItemLike;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;
import com.collectto.api_collectto.domain.shared.DomainPageResult;

public interface ItemLikeRepository {

    Optional<ItemLike> findById(UUID itemId, UUID likerId);
    ItemLike save(ItemLike itemLike);
    DomainPageResult<ItemLike> findByItemId(UUID itemId, DomainPageRequest pageRequest);
    DomainPageResult<ItemLike> findByLikerId(UUID likerId, DomainPageRequest pageRequest);
    boolean existsById(UUID itemId, UUID likerId);
    Set<UUID> findLikedItemIds(UUID likerId, List<UUID> itemIds);
    void deleteById(UUID itemId, UUID likerId);
}
