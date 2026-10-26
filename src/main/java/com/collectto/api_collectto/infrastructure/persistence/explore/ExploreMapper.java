package com.collectto.api_collectto.infrastructure.persistence.explore;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.collectto.api_collectto.domain.enums.SocialContext;
import com.collectto.api_collectto.domain.shared.DomainExploreCard;
import com.collectto.api_collectto.infrastructure.persistence.collection.CollectionJpaEntity;
import com.collectto.api_collectto.infrastructure.persistence.item.ItemJpaEntity;
import com.collectto.api_collectto.infrastructure.persistence.tag.TagJpaEntity;
import com.collectto.api_collectto.infrastructure.persistence.user.UserJpaEntity;

@Component
public final class ExploreMapper {

    public List<DomainExploreCard> toItemDomain(List<ItemJpaEntity> entities, SocialContext context) {
        return entities.stream()
            .map(entity -> {
                List<String> imageUrls = entity.getMediaUrls() != null
                    ? entity.getMediaUrls().stream()
                        .filter(url -> url != null && !url.isBlank())
                        .limit(3)
                        .toList()
                    : List.<String>of();

                UserJpaEntity user = entity.getUser();
                CollectionJpaEntity collection = entity.getCollection();

                return new DomainExploreCard(
                    entity.getId(),
                    context,
                    entity.getName(),
                    entity.getDescription(),
                    imageUrls,
                    entity.getTags() != null ? entity.getTags().stream().map(TagJpaEntity::getName).toList() : List.of(),
                    user != null ? user.getId() : null,
                    user != null ? user.getUsername() : null,
                    user != null ? user.getProfilePictureUrl() : null,
                    collection != null ? collection.getId() : null,
                    collection != null ? collection.getName() : null,
                    entity.getCreatedAt()
                );
            })
            .filter(card -> !card.imageUrls().isEmpty())
            .toList();
    }

    public List<DomainExploreCard> toCollectionDomain(List<CollectionJpaEntity> entities, Map<UUID, List<String>> medias, SocialContext context) {
        return entities.stream()
            .map(entity -> {
                List<String> itemMedias = medias != null
                    ? medias.getOrDefault(entity.getId(), List.of()).stream()
                        .filter(url -> url != null && !url.isBlank())
                        .limit(3)
                        .toList()
                    : List.<String>of();

                UserJpaEntity user = entity.getUser();

                return new DomainExploreCard(
                    entity.getId(),
                    context,
                    entity.getName(),
                    entity.getDescription(),
                    itemMedias,
                    entity.getTags() != null ? entity.getTags().stream().map(TagJpaEntity::getName).toList() : List.of(),
                    user != null ? user.getId() : null,
                    user != null ? user.getUsername() : null,
                    user != null ? user.getProfilePictureUrl() : null,
                    entity.getId(),
                    entity.getName(),
                    entity.getCreatedAt()
                );
            })
            .filter(card -> !card.imageUrls().isEmpty())
            .toList();
    }
}
