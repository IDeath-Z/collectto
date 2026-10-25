package com.collectto.api_collectto.application.usecases.notification;

import java.util.UUID;

import com.collectto.api_collectto.domain.ports.NotificationRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class MarkAllNotificationsAsReadUseCase {

    private final NotificationRepository notificationRepository;

    public record Input(UUID recipientId) {}

    public void execute(Input input) {
        notificationRepository.markAllAsReadByRecipientId(input.recipientId());
    }
}
