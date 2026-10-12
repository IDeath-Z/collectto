package com.collectto.api_collectto.presentation.controllers;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.collectto.api_collectto.application.usecases.chat.ClearChatHistoryUseCase;
import com.collectto.api_collectto.application.usecases.chat.CreateMessageUseCase;
import com.collectto.api_collectto.application.usecases.chat.FetchChatHistoryUseCase;
import com.collectto.api_collectto.application.usecases.chat.FetchUserChatsUseCase;
import com.collectto.api_collectto.infrastructure.persistence.shared.TransactionalProxy;
import com.collectto.api_collectto.infrastructure.security.SecurityUserDetails;
import com.collectto.api_collectto.presentation.dto.chat.ChatHistoryResponse;
import com.collectto.api_collectto.presentation.dto.chat.CreateMessageRequest;
import com.collectto.api_collectto.presentation.dto.chat.CreateMessageResponse;
import com.collectto.api_collectto.presentation.dto.chat.UserChatsResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chats")
public final class ChatController {

    private final CreateMessageUseCase createMessageUseCase;
    private final ClearChatHistoryUseCase clearChatHistoryUseCase;
    private final FetchChatHistoryUseCase fetchChatHistoryUseCase;
    private final FetchUserChatsUseCase fetchUserChatsUseCase;
    private final TransactionalProxy transactionalProxy;

    @PostMapping("/{otherUserId}/messages")
    @Operation(summary = "Send a message", description = "Sends a text message to another user. The chat is created on the first message and reactivated for both users if it had been cleared.")
    public ResponseEntity<CreateMessageResponse> createMessage(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID otherUserId,
            @RequestBody @Valid CreateMessageRequest request) {
        UUID senderId = userDetails.getUser().getId();

        var output = transactionalProxy.execute(() -> createMessageUseCase.execute(
            new CreateMessageUseCase.Input(senderId, otherUserId, request.content())
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CreateMessageResponse(
                output.id(),
                output.senderId(),
                output.content(),
                output.createdAt()
            ));
    }

    @GetMapping("/{otherUserId}/messages")
    @Operation(summary = "Fetch chat history", description = "Retrieves the messages of the chat with another user, newest first, starting from the point the requester last cleared the chat.")
    public ResponseEntity<ChatHistoryResponse> getChatHistory(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID otherUserId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        UUID requesterId = userDetails.getUser().getId();

        var output = transactionalProxy.executeReadOnly(() -> fetchChatHistoryUseCase.execute(
            new FetchChatHistoryUseCase.Input(requesterId, otherUserId, page, size)
        ));

        return ResponseEntity.ok(new ChatHistoryResponse(
            output.messages().stream()
                .map(message -> new ChatHistoryResponse.MessageResponse(
                    message.id(),
                    message.senderId(),
                    message.content(),
                    message.createdAt(),
                    message.readAt()
                ))
                .toList(),
            output.hasNext()
        ));
    }

    @GetMapping
    @Operation(summary = "Fetch user chats", description = "Retrieves the chats of the authenticated user, most recent activity first, each with the other participant and the last message.")
    public ResponseEntity<UserChatsResponse> getUserChats(@AuthenticationPrincipal SecurityUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        UUID requesterId = userDetails.getUser().getId();

        var output = transactionalProxy.executeReadOnly(() -> fetchUserChatsUseCase.execute(
            new FetchUserChatsUseCase.Input(requesterId, page, size)
        ));

        return ResponseEntity.ok(new UserChatsResponse(
            output.chats().stream()
                .map(chat -> new UserChatsResponse.ChatSummaryResponse(
                    new UserChatsResponse.OtherUserResponse(
                        chat.otherUser().id(),
                        chat.otherUser().username(),
                        chat.otherUser().avatarUrl()
                    ),
                    chat.content(),
                    chat.readAt()
                ))
                .toList(),
            output.hasNext()
        ));
    }

    @DeleteMapping("/{otherUserId}")
    @Operation(summary = "Clear chat history", description = "Hides the chat history for the authenticated user only. The other participant keeps seeing the messages.")
    public ResponseEntity<Void> clearChatHistory(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID otherUserId) {
        UUID requesterId = userDetails.getUser().getId();

        transactionalProxy.execute(() -> clearChatHistoryUseCase.execute(
            new ClearChatHistoryUseCase.Input(requesterId, otherUserId)
        ));

        return ResponseEntity.noContent().build();
    }
}