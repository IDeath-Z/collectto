package com.collectto.api_collectto.infrastructure.realtime;

import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import lombok.extern.slf4j.Slf4j;

// Open WebSocket connections per user. One user may be connected from several devices at once (phone and web).
// Every session is wrapped in ConcurrentWebSocketSessionDecorator: a raw WebSocketSession is not thread-safe,
// and pushes for the same user can come from different request threads at the same time.
@Slf4j
@Component
public class RealtimeSessionRegistry {

    static final int MAX_SESSIONS_PER_USER = 5;
    private static final int SEND_TIME_LIMIT_MS = 10_000;
    private static final int BUFFER_SIZE_LIMIT_BYTES = 512 * 1024;

    private final Map<UUID, Deque<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();

    public void register(UUID userId, WebSocketSession rawSession) {
        WebSocketSession session = new ConcurrentWebSocketSessionDecorator(rawSession, SEND_TIME_LIMIT_MS, BUFFER_SIZE_LIMIT_BYTES);
        // compute() is atomic per key, so a concurrent unregister cannot drop the deque this session goes into.
        Deque<WebSocketSession> sessions = sessionsByUser.compute(userId, (id, current) -> {
            Deque<WebSocketSession> deque = current == null ? new ConcurrentLinkedDeque<>() : current;
            deque.addLast(session);
            return deque;
        });

        // Oldest connection goes first: usually a device that dropped without closing.
        while (sessions.size() > MAX_SESSIONS_PER_USER) {
            WebSocketSession oldest = sessions.pollFirst();
            if (oldest != null)
                closeQuietly(oldest, CloseStatus.POLICY_VIOLATION.withReason("Too many connections"));
        }
    }

    public void unregister(UUID userId, String sessionId) {
        sessionsByUser.computeIfPresent(userId, (id, sessions) -> {
            sessions.removeIf(session -> session.getId().equals(sessionId));
            return sessions.isEmpty() ? null : sessions;
        });
    }

    public boolean isOnline(UUID userId) {
        Deque<WebSocketSession> sessions = sessionsByUser.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public void sendToUser(UUID userId, String json) {
        Deque<WebSocketSession> sessions = sessionsByUser.get(userId);
        if (sessions == null)
            return;
        TextMessage message = new TextMessage(json);
        for (WebSocketSession session : sessions)
            send(userId, session, message);
    }

    public void sendToSession(UUID userId, String sessionId, String json) {
        Deque<WebSocketSession> sessions = sessionsByUser.get(userId);
        if (sessions == null)
            return;
        TextMessage message = new TextMessage(json);
        sessions.stream()
            .filter(session -> session.getId().equals(sessionId))
            .findFirst()
            .ifPresent(session -> send(userId, session, message));
    }

    // Mobile networks and proxies silently drop idle TCP connections. A ping every 25 s keeps the path open
    // and exposes dead sessions (the send fails and the session is removed).
    @Scheduled(fixedRate = 25_000)
    void heartbeat() {
        PingMessage ping = new PingMessage();
        sessionsByUser.forEach((userId, sessions) -> sessions.forEach(session -> send(userId, session, ping)));
    }

    int sessionCount(UUID userId) {
        Deque<WebSocketSession> sessions = sessionsByUser.get(userId);
        return sessions == null ? 0 : sessions.size();
    }

    private void send(UUID userId, WebSocketSession session, WebSocketMessage<?> message) {
        if (!session.isOpen()) {
            unregister(userId, session.getId());
            return;
        }
        try {
            session.sendMessage(message);
        } catch (Exception e) {
            // Slow or broken client: drop it, it will reconnect and resync through REST.
            log.debug("Dropping WebSocket session {} of user {}: {}", session.getId(), userId, e.getMessage());
            unregister(userId, session.getId());
            closeQuietly(session, CloseStatus.SESSION_NOT_RELIABLE);
        }
    }

    private void closeQuietly(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (Exception ignored) {
            // already closed
        }
    }
}
