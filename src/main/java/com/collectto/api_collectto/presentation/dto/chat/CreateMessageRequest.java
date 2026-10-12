package com.collectto.api_collectto.presentation.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateMessageRequest", description = "Payload for sending a message to another user")
public record CreateMessageRequest(
        @NotBlank @Size(max = 2000)
        @Schema(description = "Message text", example = "Hi, how are you?")
        String content
) {}