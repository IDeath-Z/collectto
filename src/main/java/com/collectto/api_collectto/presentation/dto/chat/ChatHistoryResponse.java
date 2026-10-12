package com.collectto.api_collectto.presentation.dto.chat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ChatHistoryResponse", description = "Slice of messages of a chat, newest first")
public record ChatHistoryResponse(
        @Schema(description = "List of messages")
        List<MessageResponse> messages,

        @Schema(description = "Whether there are older messages to load")
        boolean hasNext
) {
    public record MessageResponse(
            @Schema(description = "Message identifier", example = "123e4567-e89b-12d3-a456-426614174000")
            UUID id,

            @Schema(description = "Sender identifier", example = "123e4567-e89b-12d3-a456-426614174001")
            UUID senderId,

            @Schema(description = "Message text", example = "Hi, how are you?")
            String content,

            @Schema(description = "Creation timestamp", example = "2026-10-11T21:29:00Z")
            Instant createdAt,

            @Schema(description = "When the message was read, null if unread", example = "2026-10-11T21:35:00Z", nullable = true)
            Instant readAt
    ) {}
}