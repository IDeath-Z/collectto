package com.collectto.api_collectto.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.collectto.api_collectto.application.usecases.chat.ClearChatHistoryUseCase;
import com.collectto.api_collectto.application.usecases.chat.CreateMessageUseCase;
import com.collectto.api_collectto.application.usecases.chat.FetchChatHistoryUseCase;
import com.collectto.api_collectto.application.usecases.chat.FetchUserChatsUseCase;
import com.collectto.api_collectto.domain.ports.ChatRepository;
import com.collectto.api_collectto.domain.ports.MessageRepository;
import com.collectto.api_collectto.domain.ports.UserRepository;

@Configuration
public class ChatConfig {

    @Bean
    public CreateMessageUseCase createMessageUseCase(ChatRepository chatRepository, MessageRepository messageRepository) {
        return new CreateMessageUseCase(chatRepository, messageRepository);
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
}