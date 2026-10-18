package com.collectto.api_collectto.application.usecases.explore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.collectto.api_collectto.domain.enums.SortBy;
import com.collectto.api_collectto.domain.ports.ExploreRepository;
import com.collectto.api_collectto.domain.shared.DomainExploreCard;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FetchExploreUseCase {

    private final ExploreRepository exploreRepository;
    private final TagGraphExplorer tagGraphExplorer;

    public record Input(UUID requesterId, int page, int size) {}
    public record Output(List<DomainExploreCard> content, int page, int size, boolean hasNext) {}

    public Output execute(Input input) {
        DomainPageRequest pageRequest = new DomainPageRequest(input.page(), input.size() / 2, SortBy.CREATED_AT_DESC);
        List<DomainExploreCard> exploreFeed = new ArrayList<>();
        
        Set<UUID> favoriteTagIds = exploreRepository.getFavoriteTagIds(input.requesterId());
        Set<UUID> graphRecommendedTagIds = tagGraphExplorer.findRecommendedTagIds(input.requesterId());

        List<DomainExploreCard> collections = new ArrayList<>();
        Set<UUID> addedCollectionIds = new HashSet<>();

        for (DomainExploreCard card : exploreRepository.getCollectionsByUserTagsAffinity(input.requesterId(), favoriteTagIds, pageRequest)) {
            if (addedCollectionIds.add(card.id())) collections.add(card);
        }

        // [Grafo de recomendação] Tenta completar com recomendações do grafo (BFS)
        // antes de cair para o fallback de popularidade.
        int missingCollections = pageRequest.size() - collections.size();
        if (missingCollections > 0) {
            for (DomainExploreCard card : exploreRepository.getCollectionsByUserTagsAffinity(input.requesterId(), graphRecommendedTagIds, new DomainPageRequest(pageRequest.page(), missingCollections, pageRequest.sortBy()))) {
                if (addedCollectionIds.add(card.id())) collections.add(card);
            }
        }

        missingCollections = pageRequest.size() - collections.size();
        if (missingCollections > 0) {
            for (DomainExploreCard card : exploreRepository.getCollectionsByPopularity(input.requesterId(), new DomainPageRequest(pageRequest.page(), missingCollections, pageRequest.sortBy()))) {
                if (addedCollectionIds.add(card.id())) collections.add(card);
            }
        }

        missingCollections = pageRequest.size() - collections.size();
        if (missingCollections > 0) {
            for (DomainExploreCard card : exploreRepository.getCollectionsByMostRecent(input.requesterId(), new DomainPageRequest(pageRequest.page(), missingCollections, pageRequest.sortBy()))) {
                if (addedCollectionIds.add(card.id())) collections.add(card);
            }
        }
        exploreFeed.addAll(collections);

        List<DomainExploreCard> items = new ArrayList<>();
        Set<UUID> addedItemIds = new HashSet<>();

        for (DomainExploreCard card : exploreRepository.getItemsByUserTagsAffinity(input.requesterId(), favoriteTagIds, addedCollectionIds, pageRequest)) {
            if (addedItemIds.add(card.id())) items.add(card);
        }

        // [Grafo de recomendação] Mesma ideia acima, agora para itens.
        int missingItems = pageRequest.size() - items.size();
        if (missingItems > 0) {
            for (DomainExploreCard card : exploreRepository.getItemsByUserTagsAffinity(input.requesterId(), graphRecommendedTagIds, addedCollectionIds, new DomainPageRequest(pageRequest.page(), missingItems, pageRequest.sortBy()))) {
                if (addedItemIds.add(card.id())) items.add(card);
            }
        }

        missingItems = pageRequest.size() - items.size();
        if (missingItems > 0) {
            for (DomainExploreCard card : exploreRepository.getItemsByPopularity(input.requesterId(), addedCollectionIds, new DomainPageRequest(pageRequest.page(), missingItems, pageRequest.sortBy()))) {
                if (addedItemIds.add(card.id())) items.add(card);
            }
        }

        missingItems = pageRequest.size() - items.size();
        if (missingItems > 0) {
            for (DomainExploreCard card : exploreRepository.getItemsByMostRecent(input.requesterId(), addedCollectionIds, new DomainPageRequest(pageRequest.page(), missingItems, pageRequest.sortBy()))) {
                if (addedItemIds.add(card.id())) items.add(card);
            }
        }
        exploreFeed.addAll(items);

        Collections.shuffle(exploreFeed);

        boolean hasNext = exploreFeed.size() == input.size();
        
        return new Output(
            exploreFeed, 
            input.page(), 
            input.size(), 
            hasNext
        );
    }
}