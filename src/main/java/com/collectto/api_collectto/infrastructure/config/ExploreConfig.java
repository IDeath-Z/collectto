package com.collectto.api_collectto.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.collectto.api_collectto.application.usecases.explore.FetchExploreUseCase;
import com.collectto.api_collectto.application.usecases.explore.TagGraphExplorer;
import com.collectto.api_collectto.domain.ports.ExploreRepository;

@Configuration
public class ExploreConfig {

    // [Grafo de recomendação] Registra o orquestrador do BFS como bean, para que
    // o Spring saiba "montar" essa peça e entregá-la a quem precisar dela.
    @Bean
    public TagGraphExplorer tagGraphExplorer(ExploreRepository exploreRepository) {
        return new TagGraphExplorer(exploreRepository);
    }

    @Bean
    public FetchExploreUseCase fetchExploreUseCase(ExploreRepository exploreRepository, TagGraphExplorer tagGraphExplorer) {
        return new FetchExploreUseCase(exploreRepository, tagGraphExplorer);
    }
}