package com.collectto.api_collectto.domain.shared;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.collectto.api_collectto.domain.enums.SocialContext;

public record DomainExploreCard(
    UUID id,
    SocialContext context,
    String name,
    String description,
    List<String> imageUrls,
    List<String> tags,
    UUID userId,
    String username,
    String userAvatarUrl,
    UUID collectionId,
    String collectionName,
    Instant createdAt
) {
    public DomainExploreCard(
        UUID id,
        SocialContext context,
        List<String> imageUrls,
        List<String> tags
    ) {
        this(id, context, null, null, imageUrls, tags, null, null, null, null, null, null);
    }
}
