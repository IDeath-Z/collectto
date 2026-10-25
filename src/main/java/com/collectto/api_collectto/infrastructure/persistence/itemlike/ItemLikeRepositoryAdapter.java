package com.collectto.api_collectto.infrastructure.persistence.itemlike;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.collectto.api_collectto.domain.entities.ItemLike;
import com.collectto.api_collectto.domain.ports.ItemLikeRepository;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;
import com.collectto.api_collectto.domain.shared.DomainPageResult;
import com.collectto.api_collectto.infrastructure.persistence.shared.PageConverter;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public final class ItemLikeRepositoryAdapter implements ItemLikeRepository {

    private final ItemLikeJpaRepository itemLikeJpaRepository;
    private final ItemLikeMapper itemLikeMapper;

    @Override
    public Optional<ItemLike> findById(UUID itemId, UUID likerId) {
        return itemLikeJpaRepository.findById(new ItemLikeJpaId(itemId, likerId)).map(itemLikeMapper::toDomain);
    }
    @Override
    public ItemLike save(ItemLike itemLike) {
        ItemLikeJpaEntity entity = itemLikeMapper.toJpa(itemLike);
        return itemLikeMapper.toDomain(itemLikeJpaRepository.save(entity));
    }
    @Override
    public DomainPageResult<ItemLike> findByItemId(UUID itemId, DomainPageRequest pageRequest) {
        PageRequest springPage = PageConverter.toSpring(pageRequest);
        Page<ItemLikeJpaEntity> page = itemLikeJpaRepository.findByItemId(itemId, springPage);

        return PageConverter.toDomain(page, itemLikeMapper::toDomain);
    }
    @Override
    public DomainPageResult<ItemLike> findByLikerId(UUID likerId, DomainPageRequest pageRequest) {
        PageRequest springPage = PageConverter.toSpring(pageRequest);
        Page<ItemLikeJpaEntity> page = itemLikeJpaRepository.findByLikerId(likerId, springPage);

        return PageConverter.toDomain(page, itemLikeMapper::toDomain);
    }
    @Override
    public boolean existsById(UUID itemId, UUID likerId) {
        return itemLikeJpaRepository.existsById(new ItemLikeJpaId(itemId, likerId));
    }
    @Override
    public Set<UUID> findLikedItemIds(UUID likerId, List<UUID> itemIds) {
        if (itemIds.isEmpty()) return Set.of();
        return itemLikeJpaRepository.findLikedItemIds(likerId, itemIds);
    }
    @Override
    public void deleteById(UUID itemId, UUID likerId) {
        itemLikeJpaRepository.deleteById(new ItemLikeJpaId(itemId, likerId));
    }

}
