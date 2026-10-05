package com.collectto.api_collectto.domain.ports;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.collectto.api_collectto.domain.shared.DomainExploreCard;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;

public interface ExploreRepository {

    Set<UUID> getFavoriteTagIds(UUID userId);
    
    List<DomainExploreCard> getItemsByUserTagsAffinity(UUID requesterId, Set<UUID> tags, Set<UUID> excludedCollectionIds, DomainPageRequest pageRequest);
    List<DomainExploreCard> getItemsByPopularity(UUID requesterId, Set<UUID> excludedCollectionIds, DomainPageRequest pageRequest);
    List<DomainExploreCard> getItemsByMostRecent(UUID requesterId, Set<UUID> excludedCollectionIds, DomainPageRequest pageRequest);
    
    List<DomainExploreCard> getCollectionsByUserTagsAffinity(UUID requesterId, Set<UUID> tags, DomainPageRequest pageRequest);
    List<DomainExploreCard> getCollectionsByPopularity(UUID requesterId, DomainPageRequest pageRequest);
    List<DomainExploreCard> getCollectionsByMostRecent(UUID requesterId, DomainPageRequest pageRequest);

    // [Grafo de recomendação] Devolve as tags de um usuário com o peso de cada uma
    // (quantas vezes ele usa). Alimenta tanto o usuário-alvo quanto cada "vizinho".
    Map<UUID, Integer> getTagWeightsForUser(UUID userId);

    // [Grafo de recomendação] Dado um conjunto de tags, devolve os ids de outros
    // usuários (excluindo o informado) que usam pelo menos uma delas — os "vizinhos".
    Set<UUID> getNeighborUserIds(Set<UUID> tagIds, UUID excludeUserId);
}