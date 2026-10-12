package com.collectto.api_collectto.application.usecases.chat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.collectto.api_collectto.domain.entities.Chat.ChatId;
import com.collectto.api_collectto.domain.entities.Message;
import com.collectto.api_collectto.domain.enums.SortBy;
import com.collectto.api_collectto.domain.ports.ChatRepository;
import com.collectto.api_collectto.domain.ports.MessageRepository;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;
import com.collectto.api_collectto.domain.shared.DomainSlice;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FetchChatHistoryUseCase {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    public record Input(UUID requesterId, UUID otherUserId, int page, int size) {}
    public record ChatMessages(UUID id, UUID senderId, String content, Instant createdAt, Instant readAt) {}
    public record Output(List<ChatMessages> messages, boolean hasNext) {}

    public Output execute(Input input) {
        ChatId chatId = new ChatId(input.requesterId(), input.otherUserId());
        DomainPageRequest pageRequest = new DomainPageRequest(input.page(), input.size(), SortBy.CREATED_AT_DESC);

        Optional<Instant> visibleSince = chatRepository
            .findById(chatId.getLowerUserId(), chatId.getHigherUserId())
            .flatMap(chat -> chat.visibleSinceFor(input.requesterId()));

        if (visibleSince.isEmpty())
            return new Output(List.of(), false);

        DomainSlice<Message> chatSlice = messageRepository.findVisibleByUsersIds(
            chatId.getLowerUserId(), chatId.getHigherUserId(), visibleSince.get(), pageRequest);

        List<ChatMessages> messages = chatSlice.content().stream()
            .map(message -> new ChatMessages(
                message.getId(),
                message.getSenderId(),
                message.getContent(),
                message.getCreatedAt(),
                message.getReadAt()
            ))
            .toList();

        return new Output(messages, chatSlice.hasNext());
    }
}