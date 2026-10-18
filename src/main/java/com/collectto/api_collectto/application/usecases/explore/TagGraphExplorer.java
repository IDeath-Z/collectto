package com.collectto.api_collectto.application.usecases.explore;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.collectto.api_collectto.domain.ports.ExploreRepository;
import com.collectto.api_collectto.domain.services.TagAffinityRecommender;

import lombok.RequiredArgsConstructor;

// [Grafo de recomendação] Orquestra o BFS: busca as tags do usuário, acha os
// vizinhos (até MAX_NEIGHBORS), busca as tags de TODOS os vizinhos de uma vez
// (evitando N+1) e usa o TagAffinityRecommender para pontuar as tags novas.
@RequiredArgsConstructor
public final class TagGraphExplorer {

    private static final int MAX_NEIGHBORS = 15;

    private final ExploreRepository exploreRepository;

    public Set<UUID> findRecommendedTagIds(UUID userId) {
        Map<UUID, Integer> ownTagWeights = exploreRepository.getTagWeightsForUser(userId);

        if (ownTagWeights.isEmpty())
            return Set.of(); // sem tags próprias, sem como achar vizinho

        Set<UUID> neighborIds = limitNeighbors(
            exploreRepository.getNeighborUserIds(ownTagWeights.keySet(), userId)
        );

        if (neighborIds.isEmpty())
            return Set.of();

        Map<UUID, Map<UUID, Integer>> neighborsTagWeights = exploreRepository.getTagWeightsForUsers(neighborIds);

        Map<UUID, Integer> scoreboard = TagAffinityRecommender.newEmptyScoreboard();
        for (Map<UUID, Integer> neighborWeights : neighborsTagWeights.values()) {
            TagAffinityRecommender.accumulateNeighborScore(ownTagWeights, neighborWeights, scoreboard);
        }

        return scoreboard.keySet();
    }

    private Set<UUID> limitNeighbors(Set<UUID> neighborIds) {
        if (neighborIds.size() <= MAX_NEIGHBORS)
            return neighborIds;

        return neighborIds.stream().limit(MAX_NEIGHBORS).collect(Collectors.toSet());
    }
}