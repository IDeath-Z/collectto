package com.collectto.api_collectto.application.usecases.chat;

import java.util.UUID;

import com.collectto.api_collectto.domain.ports.MessageRepository;
import com.collectto.api_collectto.domain.ports.MessageRepository.UnreadTotals;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FetchUnreadTotalsUseCase {

    private final MessageRepository messageRepository;

    public record Input(UUID requesterId) {}
    public record Output(long unreadChats, long unreadMessages) {}

    public Output execute(Input input) {
        UnreadTotals totals = messageRepository.countUnreadTotals(input.requesterId());
        return new Output(totals.chats(), totals.messages());
    }
}
