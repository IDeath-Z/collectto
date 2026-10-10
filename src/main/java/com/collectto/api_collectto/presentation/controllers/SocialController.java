package com.collectto.api_collectto.presentation.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collectto.api_collectto.application.usecases.explore.FetchExploreUseCase;
import com.collectto.api_collectto.application.usecases.feed.FetchFeedUseCase;
import com.collectto.api_collectto.application.usecases.search.FetchGlobalSearchUseCase;
import com.collectto.api_collectto.application.usecases.search.FetchTagSearchUseCase;
import com.collectto.api_collectto.infrastructure.persistence.shared.TransactionalProxy;
import com.collectto.api_collectto.infrastructure.security.SecurityUserDetails;
import com.collectto.api_collectto.presentation.dto.item.ItemResponse;
import com.collectto.api_collectto.presentation.dto.social.ExploreCardsResponse;
import com.collectto.api_collectto.presentation.dto.social.FeedResponse;
import com.collectto.api_collectto.presentation.dto.social.GlobalSearchResponse;
import com.collectto.api_collectto.presentation.dto.social.TagSearchResponse;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/social")
public final class SocialController {

    private final FetchExploreUseCase fetchExploreUseCase;
    private final FetchFeedUseCase fetchFeedUseCase;
    private final FetchGlobalSearchUseCase globalSearchUseCase;
    private final FetchTagSearchUseCase fetchTagSearchUseCase;
    private final TransactionalProxy transactionalProxy;

    @GetMapping("/explore")
    @Operation(summary = "Explore public items or collections", description = "Fetches a paginated list of cards.")
    public ResponseEntity<ExploreCardsResponse> getExplore(@AuthenticationPrincipal SecurityUserDetails userDetails,  @RequestParam(defaultValue = "0") int page, 
        @RequestParam(defaultValue = "10") int size) {
        UUID userId = userDetails.getUser().getId();

        var output = transactionalProxy.execute(() -> fetchExploreUseCase.execute(new FetchExploreUseCase.Input(userId, page, size)));

        return ResponseEntity.ok(new ExploreCardsResponse(
            output.content().stream().map(card -> new ExploreCardsResponse.ExploreCardSummary(
                card.id(),
                card.context(),
                card.imageUrls(),
                card.tags()
            )).toList(),
            output.size(),
            output.page(),
            output.hasNext()
        ));
    }

    @GetMapping("/feed")
    @Operation(summary = "Fetch user's feed", description = "Retrieves the user's feed.")
    public ResponseEntity<FeedResponse> getFeed(@AuthenticationPrincipal SecurityUserDetails userDetails,  @RequestParam(defaultValue = "0") int page, 
        @RequestParam(defaultValue = "10") int size) {
        UUID userId = userDetails.getUser().getId();

        var output = transactionalProxy.execute(() -> fetchFeedUseCase.execute(new FetchFeedUseCase.Input(userId, page, size)));

        return ResponseEntity.ok(new FeedResponse(
            output.content().stream().map(feed -> new FeedResponse.FeedSummary(
                new FeedResponse.FeedSourceSummary(
                    feed.source().id(),
                    feed.source().username(),
                    feed.source().collectionName(),
                    feed.source().avatarUrl(),
                    feed.source().context()
                ),
                new ItemResponse(
                    feed.item().getId(), 
                    feed.item().getCollectionId(),
                    feed.item().getUserId(),
                    feed.item().getName(),
                    feed.item().getDescription(),
                    feed.item().getAcquisitionDate(),
                    feed.item().getLastUsedDate(),
                    feed.item().getMediaURLs(),
                    feed.item().getAttributes(),
                    feed.item().getLikesCount(),
                    feed.item().getCommentsCount(),
                    feed.item().getTags(),
                    feed.item().isActive(),
                    feed.item().getCreatedAt(),
                    feed.item().getUpdatedAt()
                )
            )).toList(),
            output.size(),
            output.page(),
            output.hasNext()
        ));
    }

    @GetMapping("/search")
    @Operation(summary = "Global search", description = "Searches for users, collections, and items based on a query string.")
    public ResponseEntity<GlobalSearchResponse> search(@RequestParam(value = "term", defaultValue = "") String query, @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {

        var output = transactionalProxy.execute(() -> 
            globalSearchUseCase.execute(new FetchGlobalSearchUseCase.Input(query, page, size))
        );

        return ResponseEntity.ok(new GlobalSearchResponse(
            List.copyOf(output.content()),
            output.page(),
            output.size(),
            output.hasNext()
        ));
    }

    @GetMapping("/search/by-tag")
    @Operation(summary = "Search by tag", description = "Searches for users, collections, and items based on a tag.")
    public ResponseEntity<TagSearchResponse> searchByTag(@RequestParam(value = "term", defaultValue = "") String query, @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "5") int size) {

        var output = transactionalProxy.execute(() -> 
            fetchTagSearchUseCase.execute(new FetchTagSearchUseCase.Input(query, page, size))
        );

        return ResponseEntity.ok(new TagSearchResponse(
            output.content().stream()
                .map(tag -> new TagSearchResponse.TagSummaryResponse(tag.id(), tag.name(), tag.usageCount()))
                .toList(),
            output.totalPages(),
            output.totalElements(),
            output.currentPage()
        ));
    }
}
