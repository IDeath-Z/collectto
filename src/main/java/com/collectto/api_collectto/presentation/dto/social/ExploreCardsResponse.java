package com.collectto.api_collectto.presentation.dto.social;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.collectto.api_collectto.domain.enums.SocialContext;

import io.swagger.v3.oas.annotations.media.Schema;

public record ExploreCardsResponse(
    @Schema(description = "List of explore cards matching the request criteria")
    List<ExploreCardSummary> content,

    @Schema(description = "Number of elements requested per page")
    int size,

    @Schema(description = "Current page number")
    int currentPage,

    @Schema(description = "Defines if there is a next page available")
    boolean hasNext
) {
    public record ExploreCardSummary(
        @Schema(description = "Unique identifier for the explore card, can be an collectionId or an itemId", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Context of the explore card, either ITEM or COLLECTION")
        SocialContext context,

        @Schema(description = "Display name of the collection or item", example = "Action Figures Anos 90")
        String name,

        @Schema(description = "Optional description of the collection or item")
        String description,

        @Schema(description = "List of up to 3 image URLs associated with the card")
        List<String> imageUrls,

        @Schema(description = "List of tags associated with the card")
        List<String> tags,

        @Schema(description = "Identifier of the owner user", example = "123e4567-e89b-12d3-a456-426614174001")
        UUID userId,

        @Schema(description = "Username of the owner user", example = "colecionador_retro")
        String username,

        @Schema(description = "Optional profile picture URL of the owner user")
        String userAvatarUrl,

        @Schema(description = "Identifier of the collection (equals id for COLLECTION context, or parent collectionId for ITEM context)")
        UUID collectionId,

        @Schema(description = "Name of the collection associated with the card")
        String collectionName,

        @Schema(description = "Creation timestamp of the collection or item")
        Instant createdAt
    ) {
        public ExploreCardSummary(
            UUID id,
            SocialContext context,
            List<String> imageUrls,
            List<String> tags
        ) {
            this(id, context, null, null, imageUrls, tags, null, null, null, null, null, null);
        }
    }
}
