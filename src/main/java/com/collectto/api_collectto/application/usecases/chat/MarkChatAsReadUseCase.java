package com.collectto.api_collectto.application.usecases.chat;

import java.time.Instant;
import java.util.UUID;

import com.collectto.api_collectto.application.exceptions.ResourceNotFoundException;
import com.collectto.api_collectto.domain.entities.Chat.ChatId;
import com.collectto.api_collectto.domain.ports.ChatEventPublisher;
import com.collectto.api_collectto.domain.ports.ChatRepository;
import com.collectto.api_collectto.domain.ports.MessageRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class MarkChatAsReadUseCase {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ChatEventPublisher chatEventPublisher;

    public record Input(UUID requesterId, UUID otherUserId) {}

    // Marks as read every unread message the other user sent to the requester. Idempotent.
    public void execute(Input input) {
        ChatId chatId = new ChatId(input.requesterId(), input.otherUserId());

        if (!chatRepository.existsById(chatId.getLowerUserId(), chatId.getHigherUserId()))
            throw new ResourceNotFoundException("Chat not found with id: " + chatId.getLowerUserId() + " and " + chatId.getHigherUserId());

        Instant readAt = Instant.now();
        int updated = messageRepository.markAllAsReadFromSender(
            chatId.getLowerUserId(), chatId.getHigherUserId(), input.otherUserId(), readAt);

        // Nothing changed: no event, so repeated calls (every time the screen opens) cost one UPDATE and stay silent.
        if (updated > 0)
            chatEventPublisher.messagesRead(input.requesterId(), input.otherUserId(), readAt);
    }
}
