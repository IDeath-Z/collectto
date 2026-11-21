package com.collectto.api_collectto.infrastructure.realtime;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import com.collectto.api_collectto.domain.ports.MessageRepository;
import com.collectto.api_collectto.domain.ports.MessageRepository.UnreadTotals;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

// Turns chat events into WebSocket frames. AFTER_COMMIT: a client that receives "message.created" and
// immediately calls GET /chats will find the message; a rolled-back request pushes nothing.
// Delivery is best effort: a failure here is logged and never breaks the REST request that caused it.
@Slf4j
@Component
public class ChatRealtimeListener {

    record MessagePayload(UUID id, UUID senderId, String content, Instant createdAt) {}
    record UnreadPayload(long unreadChats, long unreadMessages) {}
    record MessageCreatedPayload(UUID otherUserId, MessagePayload message, UnreadPayload unread) {}
    record MessagesReadPayload(UUID otherUserId, Instant readAt) {}
    record UnreadUpdatedPayload(UnreadPayload unread) {}

    private final RealtimeSessionRegistry registry;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate readOnlyNewTransaction;

    public ChatRealtimeListener(RealtimeSessionRegistry registry, MessageRepository messageRepository,
            ObjectMapper objectMapper, PlatformTransactionManager transactionManager) {
        this.registry = registry;
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
        // The original transaction is already committed; unread totals are read in a fresh one.
        this.readOnlyNewTransaction = new TransactionTemplate(transactionManager);
        this.readOnlyNewTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.readOnlyNewTransaction.setReadOnly(true);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onMessageCreated(ChatRealtimeEvents.MessageCreated event) {
        MessagePayload message = new MessagePayload(event.messageId(), event.senderId(), event.content(), event.createdAt());
        // Recipient: new message plus fresh badge totals.
        push(event.recipientId(), "message.created", () ->
            new MessageCreatedPayload(event.senderId(), message, unreadOf(event.recipientId())));
        // Sender: other devices of the same user see the message they just sent.
        push(event.senderId(), "message.created", () ->
            new MessageCreatedPayload(event.recipientId(), message, unreadOf(event.senderId())));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onMessagesRead(ChatRealtimeEvents.MessagesRead event) {
        // Sender: "your messages to readerId were read at readAt" (the read receipt).
        push(event.senderId(), "messages.read", () -> new MessagesReadPayload(event.readerId(), event.readAt()));
        // Reader: the badge on all their devices goes down.
        push(event.readerId(), "unread.updated", () -> new UnreadUpdatedPayload(unreadOf(event.readerId())));
    }

    private void push(UUID userId, String type, java.util.function.Supplier<Object> payload) {
        if (!registry.isOnline(userId))
            return; // offline users cost no query: they resync through REST on reconnect
        try {
            registry.sendToUser(userId, RealtimeEnvelope.json(objectMapper, type, payload.get()));
        } catch (Exception e) {
            log.warn("Realtime push '{}' to user {} failed", type, userId, e);
        }
    }

    private UnreadPayload unreadOf(UUID userId) {
        UnreadTotals totals = readOnlyNewTransaction.execute(status -> messageRepository.countUnreadTotals(userId));
        return totals == null ? new UnreadPayload(0, 0) : new UnreadPayload(totals.chats(), totals.messages());
    }
}
