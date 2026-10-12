package com.collectto.api_collectto.presentation.dto.chat;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateMessageResponse", description = "Message that was just created")
public record CreateMessageResponse(
        @Schema(description = "Message identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Sender identifier", example = "123e4567-e89b-12d3-a456-426614174001")
        UUID senderId,

        @Schema(description = "Message text", example = "Hi, how are you?")
        String content,

        @Schema(description = "Creation timestamp", example = "2026-10-11T21:29:00Z")
        Instant createdAt
) {}