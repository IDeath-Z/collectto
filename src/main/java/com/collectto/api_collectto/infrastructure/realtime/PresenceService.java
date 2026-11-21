package com.collectto.api_collectto.infrastructure.realtime;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import com.collectto.api_collectto.domain.entities.Chat.ChatId;
import com.collectto.api_collectto.domain.ports.ChatRepository;
import com.collectto.api_collectto.domain.ports.PresenceReader;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

// "Online" and "typing", held by the server so clients need no timers.
//
// Online: a user is online from the first open connection until GRACE after the last one closes.
// The grace period absorbs network blips (elevator, 4G to Wi-Fi) so the label does not flicker.
//
// Typing: starts with typing.start and lasts until the message is sent, the client sends typing.stop,
// or the user goes offline. No expiry by time, on purpose (product decision of 21/11/2026).
//
// Visibility: presence and typing are only shown to people who share a chat with the user.
// State lives in memory: valid for a single API instance (see P20 for scaling).
@Slf4j
@Component
public class PresenceService implements PresenceReader {

    static final Duration OFFLINE_GRACE = Duration.ofSeconds(10);
    static final int MAX_TYPING_TARGETS = 20;

    record PresencePayload(UUID userId, boolean online) {}
    record TypingPayload(UUID userId, boolean typing) {}

    private final RealtimeSessionRegistry registry;
    private final ChatRepository chatRepository;
    private final ObjectMapper objectMapper;
    private final TaskScheduler taskScheduler;

    private final Set<UUID> onlineUsers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, ScheduledFuture<?>> pendingOffline = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> typingTargetsByUser = new ConcurrentHashMap<>();

    public PresenceService(RealtimeSessionRegistry registry, ChatRepository chatRepository,
            ObjectMapper objectMapper, TaskScheduler taskScheduler) {
        this.registry = registry;
        this.chatRepository = chatRepository;
        this.objectMapper = objectMapper;
        this.taskScheduler = taskScheduler;
    }

    // ---- PresenceReader (used by REST, for screens that open after the fact) ----

    @Override
    public boolean isOnline(UUID userId) {
        return onlineUsers.contains(userId);
    }

    @Override
    public boolean isTyping(UUID typerId, UUID targetId) {
        Set<UUID> targets = typingTargetsByUser.get(typerId);
        return targets != null && targets.contains(targetId);
    }

    // ---- connection lifecycle (called by ChatWebSocketHandler) ----

    public void onConnected(UUID userId) {
        ScheduledFuture<?> pending = pendingOffline.remove(userId);
        if (pending != null)
            pending.cancel(false); // came back inside the grace period: nobody ever saw "offline"

        if (onlineUsers.add(userId)) // only the first device announces
            broadcastToPartners(userId, "presence", new PresencePayload(userId, true));
    }

    public void onDisconnected(UUID userId) {
        if (registry.isOnline(userId))
            return; // another device is still connected
        pendingOffline.compute(userId, (id, previous) -> {
            if (previous != null)
                previous.cancel(false);
            return taskScheduler.schedule(() -> goOfflineIfStillDisconnected(userId), Instant.now().plus(OFFLINE_GRACE));
        });
    }

    void goOfflineIfStillDisconnected(UUID userId) {
        pendingOffline.remove(userId);
        if (registry.isOnline(userId) || !onlineUsers.remove(userId))
            return;

        Set<UUID> typingTargets = typingTargetsByUser.remove(userId);
        if (typingTargets != null)
            typingTargets.forEach(target -> push(target, "typing", new TypingPayload(userId, false)));

        broadcastToPartners(userId, "presence", new PresencePayload(userId, false));
    }

    // ---- typing (frames sent by the client) ----

    public void typingStarted(UUID typerId, UUID targetId) {
        if (typerId.equals(targetId) || isTyping(typerId, targetId))
            return; // repeated start: no query, no event
        if (!shareAChat(typerId, targetId))
            return; // only to people you already talk to

        Set<UUID> targets = typingTargetsByUser.computeIfAbsent(typerId, id -> ConcurrentHashMap.newKeySet());
        if (targets.size() >= MAX_TYPING_TARGETS)
            return;
        if (targets.add(targetId))
            push(targetId, "typing", new TypingPayload(typerId, true));
    }

    public void typingStopped(UUID typerId, UUID targetId) {
        Set<UUID> targets = typingTargetsByUser.get(typerId);
        if (targets != null && targets.remove(targetId))
            push(targetId, "typing", new TypingPayload(typerId, false));
    }

    // Sending the message ends the typing indicator (called after the message is committed).
    public void messageSent(UUID senderId, UUID recipientId) {
        typingStopped(senderId, recipientId);
    }

    // ---- helpers ----

    private boolean shareAChat(UUID a, UUID b) {
        ChatId chatId = new ChatId(a, b);
        return chatRepository.existsById(chatId.getLowerUserId(), chatId.getHigherUserId());
    }

    private void broadcastToPartners(UUID userId, String type, Object payload) {
        try {
            String json = RealtimeEnvelope.json(objectMapper, type, payload);
            for (UUID partnerId : chatRepository.findPartnerIds(userId))
                if (registry.isOnline(partnerId))
                    registry.sendToUser(partnerId, json);
        } catch (Exception e) {
            log.warn("Presence broadcast '{}' for user {} failed", type, userId, e);
        }
    }

    private void push(UUID userId, String type, Object payload) {
        if (registry.isOnline(userId))
            registry.sendToUser(userId, RealtimeEnvelope.json(objectMapper, type, payload));
    }
}
