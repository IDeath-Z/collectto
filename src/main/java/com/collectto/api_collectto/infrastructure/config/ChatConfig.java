package com.collectto.api_collectto.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.collectto.api_collectto.application.usecases.chat.ClearChatHistoryUseCase;
import com.collectto.api_collectto.application.usecases.chat.CreateMessageUseCase;
import com.collectto.api_collectto.application.usecases.chat.FetchChatHistoryUseCase;
import com.collectto.api_collectto.application.usecases.chat.FetchUnreadTotalsUseCase;
import com.collectto.api_collectto.application.usecases.chat.FetchUserChatsUseCase;
import com.collectto.api_collectto.application.usecases.chat.MarkChatAsReadUseCase;
import com.collectto.api_collectto.domain.ports.ChatEventPublisher;
import com.collectto.api_collectto.domain.ports.ChatRepository;
import com.collectto.api_collectto.domain.ports.MessageRepository;
import com.collectto.api_collectto.domain.ports.UserRepository;

@Configuration
public class ChatConfig {

    @Bean
    public CreateMessageUseCase createMessageUseCase(ChatRepository chatRepository, MessageRepository messageRepository, ChatEventPublisher chatEventPublisher) {
        return new CreateMessageUseCase(chatRepository, messageRepository, chatEventPublisher);
    }

    @Bean
    public ClearChatHistoryUseCase clearChatHistoryUseCase(ChatRepository chatRepository) {
        return new ClearChatHistoryUseCase(chatRepository);
    }

    @Bean
    public FetchChatHistoryUseCase fetchChatHistoryUseCase(ChatRepository chatRepository, MessageRepository messageRepository) {
        return new FetchChatHistoryUseCase(chatRepository, messageRepository);
    }

    @Bean
    public FetchUserChatsUseCase fetchUserChatsUseCase(ChatRepository chatRepository, MessageRepository messageRepository, UserRepository userRepository) {
        return new FetchUserChatsUseCase(chatRepository, messageRepository, userRepository);
    }

    @Bean
    public MarkChatAsReadUseCase markChatAsReadUseCase(ChatRepository chatRepository, MessageRepository messageRepository, ChatEventPublisher chatEventPublisher) {
        return new MarkChatAsReadUseCase(chatRepository, messageRepository, chatEventPublisher);
    }

    @Bean
    public FetchUnreadTotalsUseCase fetchUnreadTotalsUseCase(MessageRepository messageRepository) {
        return new FetchUnreadTotalsUseCase(messageRepository);
    }
}
