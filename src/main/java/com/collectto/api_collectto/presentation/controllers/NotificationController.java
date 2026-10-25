package com.collectto.api_collectto.presentation.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collectto.api_collectto.application.usecases.notification.FetchNotificationsUseCase;
import com.collectto.api_collectto.application.usecases.notification.MarkAllNotificationsAsReadUseCase;
import com.collectto.api_collectto.application.usecases.notification.MarkNotificationAsReadUseCase;
import com.collectto.api_collectto.domain.enums.SortBy;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;
import com.collectto.api_collectto.infrastructure.persistence.shared.TransactionalProxy;
import com.collectto.api_collectto.infrastructure.security.SecurityUserDetails;
import com.collectto.api_collectto.presentation.dto.notification.NotificationPageResponse;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public final class NotificationController {

    private final FetchNotificationsUseCase fetchNotificationsUseCase;
    private final MarkNotificationAsReadUseCase markNotificationAsReadUseCase;
    private final MarkAllNotificationsAsReadUseCase markAllNotificationsAsReadUseCase;
    private final TransactionalProxy transactionalProxy;

    @GetMapping()
    @Operation(summary = "Fetch paginated notifications for the authenticated user", description = "Retrieves a paginated list of notifications for the authenticated user. Supports pagination and sorting.")
    public ResponseEntity<NotificationPageResponse> getNotifications(@AuthenticationPrincipal SecurityUserDetails userDetails,
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "CREATED_AT_DESC") SortBy sortBy) {
        UUID userId = userDetails.getUser().getId();

        var output = transactionalProxy.execute(() -> fetchNotificationsUseCase.execute(
            new FetchNotificationsUseCase.Input(
                userId, 
                new DomainPageRequest(page, size, sortBy)
            )
        ));
            
        return ResponseEntity.ok(new NotificationPageResponse(
            output.notifications().stream()
                .map(notification -> new NotificationPageResponse.NotificationSummary(
                    notification.id(), 
                    notification.recipientId(), 
                    notification.actor() != null ? new NotificationPageResponse.ActorSummary(
                        notification.actor().id(), 
                        notification.actor().username(), 
                        notification.actor().profilePictureUrl()
                    ) : null,
                    notification.context(), 
                    notification.reference() != null ? new NotificationPageResponse.ReferenceSummary(
                        notification.reference().id(),
                        notification.reference().parentId(),
                        notification.reference().referenceImageUrl()
                    ) : null,
                    notification.isRead(), 
                    notification.createdAt()
                ))
                .toList(), 
            output.totalPages(), 
            output.totalElements(), 
            output.currentPage()
        ));
    } 

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark a notification as read", description = "Marks a single notification of the authenticated user as read.")
    public ResponseEntity<Void> markAsRead(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID notificationId) {
        UUID requesterId = userDetails.getUser().getId();

        transactionalProxy.execute(() -> markNotificationAsReadUseCase.execute(
            new MarkNotificationAsReadUseCase.Input(requesterId, notificationId)
        ));

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Marks all unread notifications of the authenticated user as read.")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal SecurityUserDetails userDetails) {
        UUID userId = userDetails.getUser().getId();

        transactionalProxy.execute(() -> markAllNotificationsAsReadUseCase.execute(
            new MarkAllNotificationsAsReadUseCase.Input(userId)
        ));

        return ResponseEntity.noContent().build();
    }
}
