package com.collectto.api_collectto.application.usecases.chat;

import java.util.UUID;

import com.collectto.api_collectto.domain.entities.Chat.ChatId;
import com.collectto.api_collectto.domain.ports.ChatRepository;
import com.collectto.api_collectto.domain.ports.PresenceReader;

import lombok.RequiredArgsConstructor;

// What a chat screen shows when it opens: is the other user online, are they typing to me.
// Without a chat between the two, the answer is always false: presence is not visible to strangers.
@RequiredArgsConstructor
public final class FetchChatStatusUseCase {

    private final ChatRepository chatRepository;
    private final PresenceReader presenceReader;

    public record Input(UUID requesterId, UUID otherUserId) {}
    public record Output(boolean online, boolean typing) {}

    public Output execute(Input input) {
        ChatId chatId = new ChatId(input.requesterId(), input.otherUserId());
        if (!chatRepository.existsById(chatId.getLowerUserId(), chatId.getHigherUserId()))
            return new Output(false, false);

        return new Output(
            presenceReader.isOnline(input.otherUserId()),
            presenceReader.isTyping(input.otherUserId(), input.requesterId())
        );
    }
}
