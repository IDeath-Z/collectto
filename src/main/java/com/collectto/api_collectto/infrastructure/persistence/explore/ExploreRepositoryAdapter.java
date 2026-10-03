package com.collectto.api_collectto.infrastructure.persistence.explore;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.collectto.api_collectto.domain.enums.SocialContext;
import com.collectto.api_collectto.domain.enums.Visibility;
import com.collectto.api_collectto.domain.ports.ExploreRepository;
import com.collectto.api_collectto.domain.shared.DomainExploreCard;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;
import com.collectto.api_collectto.infrastructure.persistence.collection.CollectionJpaEntity;
import com.collectto.api_collectto.infrastructure.persistence.collection.CollectionJpaRepository;
import com.collectto.api_collectto.infrastructure.persistence.item.CollectionMediaProjection;
import com.collectto.api_collectto.infrastructure.persistence.item.ItemJpaRepository;
import com.collectto.api_collectto.infrastructure.persistence.shared.PageConverter;
import com.collectto.api_collectto.infrastructure.persistence.tag.TagJpaEntity;
import com.collectto.api_collectto.infrastructure.persistence.tag.TagJpaRepository;
import com.collectto.api_collectto.infrastructure.persistence.tag.TagWeightProjection;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;

import com.collectto.api_collectto.infrastructure.persistence.tag.TagWeightProjection;

@Service
@RequiredArgsConstructor
public class ExploreRepositoryAdapter implements ExploreRepository {

    private final TagJpaRepository tagRepository;
    private final ItemJpaRepository itemRepository;
    private final CollectionJpaRepository collectionRepository;
    private final ExploreMapper exploreMapper;
    
    @Override
    public Set<UUID> getFavoriteTagIds(UUID userId) {
        return tagRepository.findFavoriteTagsByUserId(userId).stream()
            .map(TagJpaEntity::getId)
            .collect(Collectors.toSet());
    }

    @Override
    public List<DomainExploreCard> getItemsByUserTagsAffinity(UUID requesterId, Set<UUID> tags, Set<UUID> excludedCollectionIds, DomainPageRequest pageRequest) {
        if (tags.isEmpty())
            return List.of();

        PageRequest springPage = PageConverter.toSpring(pageRequest);

        return exploreMapper.toItemDomain(
            itemRepository.findRecommendedByTags(tags, requesterId, normalizeExcluded(excludedCollectionIds), springPage),
            SocialContext.ITEM
        );
    }

    @Override
    public List<DomainExploreCard> getItemsByPopularity(UUID requesterId, Set<UUID> excludedCollectionIds, DomainPageRequest pageRequest) {
        PageRequest springPage = PageConverter.toSpring(pageRequest);
        return exploreMapper.toItemDomain(
            itemRepository.findTrendingItems(requesterId, normalizeExcluded(excludedCollectionIds), springPage),
            SocialContext.ITEM
        );
    }

    @Override
    public List<DomainExploreCard> getItemsByMostRecent(UUID requesterId, Set<UUID> excludedCollectionIds, DomainPageRequest pageRequest) {
        PageRequest springPage = PageConverter.toSpring(pageRequest);
        return exploreMapper.toItemDomain(
            itemRepository.findLatestItems(requesterId, normalizeExcluded(excludedCollectionIds), springPage),
            SocialContext.ITEM
        );
    }

    // NOT IN with an empty list is invalid SQL, so an unmatchable nil UUID keeps the query valid
    private Set<UUID> normalizeExcluded(Set<UUID> excludedCollectionIds) {
        return excludedCollectionIds.isEmpty() ? Set.of(new UUID(0L, 0L)) : excludedCollectionIds;
    }

    @Override
    public List<DomainExploreCard> getCollectionsByUserTagsAffinity(UUID requesterId, Set<UUID> tags, DomainPageRequest pageRequest) {
        if (tags.isEmpty()) 
            return List.of();

        PageRequest springPage = PageConverter.toSpring(pageRequest);

        List<CollectionJpaEntity> entities = collectionRepository.findRecommendedByTags(tags, Visibility.PUBLIC,requesterId, springPage);
        Set<UUID> collectionIds = entities.stream().map(CollectionJpaEntity::getId).collect(Collectors.toSet());

        return exploreMapper.toCollectionDomain(entities, mapMedias(collectionIds), SocialContext.COLLECTION);
    }

    @Override
    public List<DomainExploreCard> getCollectionsByPopularity(UUID requesterId, DomainPageRequest pageRequest) {
        PageRequest springPage = PageConverter.toSpring(pageRequest);

        List<CollectionJpaEntity> entities = collectionRepository.findTrendingCollections(Visibility.PUBLIC, requesterId, springPage);

        if (entities.isEmpty())
            return List.of();

        Set<UUID> collectionIds = entities.stream().map(CollectionJpaEntity::getId).collect(Collectors.toSet());

        return exploreMapper.toCollectionDomain(entities, mapMedias(collectionIds), SocialContext.COLLECTION);
    }

    @Override
    public List<DomainExploreCard> getCollectionsByMostRecent(UUID requesterId, DomainPageRequest pageRequest) {
        PageRequest springPage = PageConverter.toSpring(pageRequest);

        List<CollectionJpaEntity> entities = collectionRepository.findLatestCollections(Visibility.PUBLIC, requesterId, springPage);

        if (entities.isEmpty())
            return List.of();

        Set<UUID> collectionIds = entities.stream().map(CollectionJpaEntity::getId).collect(Collectors.toSet());

        return exploreMapper.toCollectionDomain(entities, mapMedias(collectionIds), SocialContext.COLLECTION);
    }

    private Map<UUID, List<String>> mapMedias(Set<UUID> collectionIds) {
        return collectionRepository.findMediaForCollections(collectionIds).stream()
            .collect(Collectors.toMap(
                CollectionMediaProjection::getCollectionId,
                proj -> proj.getMediaUrls().stream().limit(3).toList(),
                (v1, v2) -> v1
            ));
    }

        // [Grafo de recomendação] Implementação do port: chama o TagJpaRepository
    // (que já é injetado nesta classe) e converte o resultado do banco
    // (TagWeightProjection, um formato específico do JPA) para um Map comum,
    // que é o que o domain entende — sem expor nenhum detalhe de JPA pra fora daqui.
    @Override
    public Map<UUID, Integer> getTagWeightsForUser(UUID userId) {
        return tagRepository.findTagWeightsByUserId(userId).stream()
            .collect(Collectors.toMap(TagWeightProjection::getTagId, TagWeightProjection::getWeight));
    }

    // [Grafo de recomendação] Implementação do port: busca vizinhos, mas evita
    // chamar o banco à toa se não houver nenhuma tag para procurar.
    @Override
    public Set<UUID> getNeighborUserIds(Set<UUID> tagIds, UUID excludeUserId) {
        if (tagIds.isEmpty())
            return Set.of();

        return new HashSet<>(tagRepository.findUserIdsByTagIds(tagIds, excludeUserId));
    }
}
