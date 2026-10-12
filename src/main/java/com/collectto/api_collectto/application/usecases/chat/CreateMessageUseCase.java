package com.collectto.api_collectto.application.usecases.chat;

import java.time.Instant;
import java.util.UUID;

import com.collectto.api_collectto.domain.entities.Chat;
import com.collectto.api_collectto.domain.entities.Message;
import com.collectto.api_collectto.domain.entities.Chat.ChatId;
import com.collectto.api_collectto.domain.ports.ChatRepository;
import com.collectto.api_collectto.domain.ports.MessageRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
public final class CreateMessageUseCase {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    public record Input(UUID senderId, UUID receiverId, String content) {}
    public record Output(UUID id, UUID senderId, String content, Instant createdAt) {}

    public Output execute(Input input) {
        ChatId chatId = new ChatId(input.senderId(), input.receiverId());

        Chat chat = chatRepository.findById(chatId.getLowerUserId(), chatId.getHigherUserId())
            .orElseGet(() -> chatRepository.save(Chat.createNewChat(chatId)));

        Message newMessage = Message.createMessage(chat, input.senderId(), input.content());
        chatRepository.save(chat.makeVisibleFrom(newMessage.getCreatedAt()));
        messageRepository.save(newMessage);

        return new Output(
            newMessage.getId(),
            newMessage.getSenderId(),
            newMessage.getContent(),
            newMessage.getCreatedAt()
        );
    }
}
