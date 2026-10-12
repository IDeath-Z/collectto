package com.collectto.api_collectto.application.usecases.chat;

import java.util.UUID;

import com.collectto.api_collectto.application.exceptions.ResourceNotFoundException;
import com.collectto.api_collectto.domain.entities.Chat;
import com.collectto.api_collectto.domain.entities.Chat.ChatId;
import com.collectto.api_collectto.domain.ports.ChatRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
public final class ClearChatHistoryUseCase {

    private final ChatRepository chatRepository;

    public record Input(UUID requesterId, UUID otherUserId) {}

    public void execute(Input input) {
        ChatId chatId = new ChatId(input.requesterId(), input.otherUserId());

        Chat chat = chatRepository.findById(chatId.getLowerUserId(), chatId.getHigherUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Chat not found with id: " + chatId.getLowerUserId() + " and " + chatId.getHigherUserId()));
            
        chatRepository.save(chat.clearHistoryFor(input.requesterId()));
    }

}
