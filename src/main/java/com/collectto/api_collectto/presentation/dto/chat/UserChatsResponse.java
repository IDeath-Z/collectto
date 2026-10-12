package com.collectto.api_collectto.presentation.dto.chat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserChatsResponse", description = "Slice of the user's chats, most recent activity first")
public record UserChatsResponse(
        @Schema(description = "List of chats")
        List<ChatSummaryResponse> chats,

        @Schema(description = "Whether there are more chats to load")
        boolean hasNext
) {
    public record ChatSummaryResponse(
            @Schema(description = "The other participant of the chat")
            OtherUserResponse otherUser,

            @Schema(description = "Last message text", example = "Hi, how are you?")
            String content,

            @Schema(description = "When the last message was read, null if unread", example = "2026-10-11T21:35:00Z", nullable = true)
            Instant readAt
    ) {}

    public record OtherUserResponse(
            @Schema(description = "User identifier", example = "123e4567-e89b-12d3-a456-426614174000")
            UUID id,

            @Schema(description = "Username", example = "collector 123")
            String username,

            @Schema(description = "Profile picture URL", example = "https://...", nullable = true)
            String avatarUrl
    ) {}
}