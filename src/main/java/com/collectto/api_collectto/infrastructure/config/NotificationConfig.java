package com.collectto.api_collectto.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.collectto.api_collectto.application.usecases.notification.FetchNotificationsUseCase;
import com.collectto.api_collectto.application.usecases.notification.MarkAllNotificationsAsReadUseCase;
import com.collectto.api_collectto.application.usecases.notification.MarkNotificationAsReadUseCase;
import com.collectto.api_collectto.domain.ports.CollectionRepository;
import com.collectto.api_collectto.domain.ports.ItemRepository;
import com.collectto.api_collectto.domain.ports.NotificationRepository;
import com.collectto.api_collectto.domain.ports.UserRepository;

@Configuration
public class NotificationConfig {

    @Bean
    public FetchNotificationsUseCase fetchNotificationsUseCase(NotificationRepository notificationRepository, UserRepository userRepository, 
        ItemRepository itemRepository, CollectionRepository collectionRepository) {
        return new FetchNotificationsUseCase(notificationRepository, userRepository, itemRepository, collectionRepository);
    }

    @Bean
    public MarkNotificationAsReadUseCase markNotificationAsReadUseCase(NotificationRepository notificationRepository) {
        return new MarkNotificationAsReadUseCase(notificationRepository);
    }

    @Bean
    public MarkAllNotificationsAsReadUseCase markAllNotificationsAsReadUseCase(NotificationRepository notificationRepository) {
        return new MarkAllNotificationsAsReadUseCase(notificationRepository);
    }
}
