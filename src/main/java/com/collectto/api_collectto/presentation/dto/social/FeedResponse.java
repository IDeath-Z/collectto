package com.collectto.api_collectto.presentation.dto.social;

import java.util.List;
import java.util.UUID;

import com.collectto.api_collectto.domain.enums.SocialContext;
import com.collectto.api_collectto.presentation.dto.item.ItemResponse;

import io.swagger.v3.oas.annotations.media.Schema;

public record FeedResponse(
    @Schema(description = "List of feed cards matching the request criteria")
    List<FeedSummary> content,

    @Schema(description = "Number of elements requested per page")
    int size,

    @Schema(description = "Current page number")
    int currentPage,

    @Schema(description = "Defines if there is a next page available")
    boolean hasNext
) {
    public record FeedSummary(
        @Schema(description = "Summary information about the source of the feed card, which can be either a user or a collection")
        FeedSourceSummary source,
        
        @Schema(description = "Summary information about the item associated with the feed card")
        ItemResponse item,

        @Schema(description = "Defines if the authenticated user has already liked the item", example = "true")
        boolean isLiked
    ) {}

    public record FeedSourceSummary(
        @Schema(description = "Unique identifier for the feed source, can be an userId or collectionId", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Username of the feed source, if the source is a user, if not applicable, this field will be null", example = "john_doe")
        String username,

        @Schema(description = "Name of the collection, if the source is a collection, if not applicable, this field will be null", example = "My Collection")
        String collectionName,

        @Schema(description = "URL of the avatar image for the feed source, can be a user avatar or a collection cover image")
        String avatarUrl,

        @Schema(description = "Context of the feed source, either USER or COLLECTION")
        SocialContext context
    ) {}
}
