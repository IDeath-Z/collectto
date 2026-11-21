package com.collectto.api_collectto.presentation.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UnreadTotalsResponse", description = "Unread totals of the authenticated user, for the chat badge")
public record UnreadTotalsResponse(
        @Schema(description = "Chats with at least one unread message", example = "2")
        long unreadChats,
        @Schema(description = "Unread messages across all chats", example = "5")
        long unreadMessages
) {}
