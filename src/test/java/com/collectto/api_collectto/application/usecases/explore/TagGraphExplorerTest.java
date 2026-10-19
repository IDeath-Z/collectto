package com.collectto.api_collectto.application.usecases.explore;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.collectto.api_collectto.domain.ports.ExploreRepository;
import com.collectto.api_collectto.domain.shared.DomainExploreCard;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;

class TagGraphExplorerTest {

    @Test
    void returnsEmptyWhenUserHasNoTags() {
        FakeExploreRepository fakeRepository = new FakeExploreRepository();
        fakeRepository.ownTagWeights = Map.of(); // Ana sem nenhuma tag própria

        var explorer = new TagGraphExplorer(fakeRepository);
        var result = explorer.findRecommendedTagIds(UUID.randomUUID());

        assertThat(result).isEmpty();
        assertThat(fakeRepository.neighborSearchCalled).isFalse(); // nem tentou buscar vizinho (guard clause funcionando)
    }

    @Test
    void recommendsNeighborsTagButIgnoresOwnTag() {
        UUID lego = UUID.randomUUID();
        UUID funko = UUID.randomUUID();
        UUID brunoId = UUID.randomUUID();

        FakeExploreRepository fakeRepository = new FakeExploreRepository();
        fakeRepository.ownTagWeights = Map.of(lego, 5);
        fakeRepository.neighborIds = Set.of(brunoId);
        fakeRepository.neighborsTagWeights = Map.of(brunoId, Map.of(lego, 3, funko, 4));

        var explorer = new TagGraphExplorer(fakeRepository);
        var result = explorer.findRecommendedTagIds(UUID.randomUUID());

        assertThat(result).containsExactly(funko); // só "funko" — "lego" foi ignorado, pois a Ana já tem
    }

    @Test
    void neverSendsMoreThan15NeighborsToTheBatchQuery() {
        UUID lego = UUID.randomUUID();

        FakeExploreRepository fakeRepository = new FakeExploreRepository();
        fakeRepository.ownTagWeights = Map.of(lego, 1);
        fakeRepository.neighborIds = generateFakeUserIds(20); // mais do que o teto de 15

        var explorer = new TagGraphExplorer(fakeRepository);
        explorer.findRecommendedTagIds(UUID.randomUUID());

        assertThat(fakeRepository.lastRequestedNeighborIds).hasSizeLessThanOrEqualTo(15);
    }

    private Set<UUID> generateFakeUserIds(int amount) {
        Set<UUID> ids = new HashSet<>();
        for (int i = 0; i < amount; i++) {
            ids.add(UUID.randomUUID());
        }
        return ids;
    }

    // Dublê de teste: uma versão falsa e controlável do ExploreRepository,
    // feita só para este teste, sem nenhum banco de dados real por trás.
    private static final class FakeExploreRepository implements ExploreRepository {

        Map<UUID, Integer> ownTagWeights = Map.of();
        Set<UUID> neighborIds = Set.of();
        Map<UUID, Map<UUID, Integer>> neighborsTagWeights = Map.of();

        boolean neighborSearchCalled = false;
        Set<UUID> lastRequestedNeighborIds = Set.of();

        @Override
        public Map<UUID, Integer> getTagWeightsForUser(UUID userId) {
            return ownTagWeights;
        }

        @Override
        public Set<UUID> getNeighborUserIds(Set<UUID> tagIds, UUID excludeUserId) {
            neighborSearchCalled = true;
            return neighborIds;
        }

        @Override
        public Map<UUID, Map<UUID, Integer>> getTagWeightsForUsers(Set<UUID> userIds) {
            lastRequestedNeighborIds = userIds;
            return neighborsTagWeights;
        }

        // Os métodos abaixo não são usados pelo TagGraphExplorer, então ficam
        // "vazios" só para a classe conseguir compilar (a interface exige todos).
        @Override
        public Set<UUID> getFavoriteTagIds(UUID userId) { return Set.of(); }
        @Override
        public List<DomainExploreCard> getItemsByUserTagsAffinity(UUID requesterId, Set<UUID> tags, Set<UUID> excludedCollectionIds, DomainPageRequest pageRequest) { return List.of(); }
        @Override
        public List<DomainExploreCard> getItemsByPopularity(UUID requesterId, Set<UUID> excludedCollectionIds, DomainPageRequest pageRequest) { return List.of(); }
        @Override
        public List<DomainExploreCard> getItemsByMostRecent(UUID requesterId, Set<UUID> excludedCollectionIds, DomainPageRequest pageRequest) { return List.of(); }
        @Override
        public List<DomainExploreCard> getCollectionsByUserTagsAffinity(UUID requesterId, Set<UUID> tags, DomainPageRequest pageRequest) { return List.of(); }
        @Override
        public List<DomainExploreCard> getCollectionsByPopularity(UUID requesterId, DomainPageRequest pageRequest) { return List.of(); }
        @Override
        public List<DomainExploreCard> getCollectionsByMostRecent(UUID requesterId, DomainPageRequest pageRequest) { return List.of(); }
    }
}
