package com.collectto.api_collectto.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.collectto.api_collectto.application.usecases.feed.FetchFeedUseCase;
import com.collectto.api_collectto.domain.ports.FeedRepository;
import com.collectto.api_collectto.domain.ports.ItemLikeRepository;

@Configuration
public class FeedConfig {

    @Bean
    public FetchFeedUseCase fetchFeedUseCase(FeedRepository feedRepository, ItemLikeRepository itemLikeRepository) {
        return new FetchFeedUseCase(feedRepository, itemLikeRepository);
    }
}
