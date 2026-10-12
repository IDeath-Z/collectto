package com.collectto.api_collectto.application.usecases.chat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.collectto.api_collectto.domain.entities.Chat;
import com.collectto.api_collectto.domain.entities.Message;
import com.collectto.api_collectto.domain.entities.User;
import com.collectto.api_collectto.domain.enums.SortBy;
import com.collectto.api_collectto.domain.ports.ChatRepository;
import com.collectto.api_collectto.domain.ports.MessageRepository;
import com.collectto.api_collectto.domain.ports.UserRepository;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;
import com.collectto.api_collectto.domain.shared.DomainSlice;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FetchUserChatsUseCase {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public record Input(UUID requesterId, int page, int size) {}
    public record UserInfo(UUID id, String username, String avatarUrl) {}
    public record Chats(UserInfo otherUser, String content, Instant readAt) {}
    public record Output(List<Chats> chats, boolean hasNext) {}

    public Output execute(Input input) {
        DomainPageRequest pageRequest = new DomainPageRequest(input.page(), input.size(), SortBy.CREATED_AT_DESC);

        DomainSlice<Chat> chatSlice = chatRepository.findAllVisibleByUserId(input.requesterId(), pageRequest);

        if (chatSlice.content().isEmpty())
            return new Output(List.of(), false);

        List<UUID> otherUserIds = chatSlice.content().stream()
            .map(chat -> chat.otherUserIdFor(input.requesterId()))
            .toList();

        Map<UUID, User> usersById = userRepository.findAllByIds(otherUserIds).stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

        Map<UUID, Message> lastMessageByOtherUserId = messageRepository
            .findLastMessagesByOtherUserIds(input.requesterId(), otherUserIds).stream()
            .collect(Collectors.toMap(m -> m.otherParticipantFor(input.requesterId()), Function.identity()));

        List<Chats> chats = otherUserIds.stream()
            .filter(usersById::containsKey)
            .map(otherUserId -> {
                User user = usersById.get(otherUserId);
                Message lastMessage = lastMessageByOtherUserId.get(otherUserId);

                if (lastMessage == null)
                    throw new IllegalStateException("Visible chat without messages with user " + otherUserId);

                return new Chats(
                    new UserInfo(user.getId(), user.getUsername(), user.getProfilePictureUrl()),
                    lastMessage.getContent(),
                    lastMessage.getReadAt()
                );
            })
            .toList();

        return new Output(chats, chatSlice.hasNext());
    }
}