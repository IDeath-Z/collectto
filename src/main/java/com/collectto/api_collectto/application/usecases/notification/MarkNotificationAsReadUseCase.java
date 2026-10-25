package com.collectto.api_collectto.application.usecases.notification;

import java.util.UUID;

import com.collectto.api_collectto.application.exceptions.ForbiddenActionException;
import com.collectto.api_collectto.application.exceptions.ResourceNotFoundException;
import com.collectto.api_collectto.domain.entities.Notification;
import com.collectto.api_collectto.domain.ports.NotificationRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class MarkNotificationAsReadUseCase {

    private final NotificationRepository notificationRepository;

    public record Input(UUID requesterId, UUID notificationId) {}

    public void execute(Input input) {
        Notification notification = notificationRepository.findById(input.notificationId())
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + input.notificationId()));

        if (!notification.getRecipientId().equals(input.requesterId()))
            throw new ForbiddenActionException("User does not have permission to mark this notification as read");

        if (notification.isRead())
            return;

        notificationRepository.save(notification.markAsRead());
    }
}
