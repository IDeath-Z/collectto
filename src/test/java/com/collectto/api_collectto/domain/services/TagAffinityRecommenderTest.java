package com.collectto.api_collectto.domain.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TagAffinityRecommenderTest {

    @Test
    void naoRecomendaTagQueUsuarioJaTem() {
        UUID lego = UUID.randomUUID();
        Map<UUID, Integer> ana = Map.of(lego, 5);
        Map<UUID, Integer> bruno = Map.of(lego, 3);

        var placar = TagAffinityRecommender.newEmptyScoreboard();
        TagAffinityRecommender.accumulateNeighborScore(ana, bruno, placar);

        assertThat(placar).isEmpty(); // "lego" foi ignorado, pois Ana já tem
    }

    @Test
    void recomendaTagNovaDoVizinhoComPesoCorreto() {
        UUID lego = UUID.randomUUID();
        UUID funko = UUID.randomUUID();
        Map<UUID, Integer> ana = Map.of(lego, 5);
        Map<UUID, Integer> bruno = Map.of(lego, 3, funko, 4);

        var placar = TagAffinityRecommender.newEmptyScoreboard();
        TagAffinityRecommender.accumulateNeighborScore(ana, bruno, placar);

        assertThat(placar).containsEntry(funko, 4); // "funko" apareceu com o peso do Bruno
    }
}