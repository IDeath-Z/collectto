package com.collectto.api_collectto.presentation.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ChatStatusResponse", description = "Live state of the other participant, for when the chat screen opens. Changes after that arrive through the WebSocket")
public record ChatStatusResponse(
        @Schema(description = "The other participant is online", example = "true")
        boolean online,
        @Schema(description = "The other participant is typing a message to you", example = "false")
        boolean typing
) {}
