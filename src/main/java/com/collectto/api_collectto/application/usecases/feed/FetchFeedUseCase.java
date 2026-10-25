package com.collectto.api_collectto.application.usecases.feed;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.collectto.api_collectto.domain.entities.Item;
import com.collectto.api_collectto.domain.enums.SortBy;
import com.collectto.api_collectto.domain.ports.FeedRepository;
import com.collectto.api_collectto.domain.ports.ItemLikeRepository;
import com.collectto.api_collectto.domain.shared.DomainFeedCard;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FetchFeedUseCase {

    private final FeedRepository feedRepository;
    private final ItemLikeRepository itemLikeRepository;

    public record Input(UUID userId, int page, int size) {}
    public record FeedCardSummary(DomainFeedCard.FeedSource source, Item item, boolean isLiked) {}
    public record Output(List<FeedCardSummary> content, int page, int size, boolean hasNext) {}

    public Output execute(Input input) {
        DomainPageRequest pageRequest = new DomainPageRequest(input.page(), input.size(), SortBy.CREATED_AT_DESC);
        
        List<DomainFeedCard> feed = feedRepository.getFeed(input.userId(), pageRequest);

        List<UUID> itemIds = feed.stream()
            .map(card -> card.item().getId())
            .toList();

        Set<UUID> likedItemIds = itemLikeRepository.findLikedItemIds(input.userId(), itemIds);

        List<FeedCardSummary> content = feed.stream()
            .map(card -> new FeedCardSummary(card.source(), card.item(), likedItemIds.contains(card.item().getId())))
            .toList();

        boolean hasNext = feed.size() == input.size();

        return new Output(content, input.page(), input.size(), hasNext);
    }
}
