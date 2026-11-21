package com.collectto.api_collectto.infrastructure.realtime;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Server-to-client channel. Writes (send message, mark as read) stay in REST, which already validates,
// authorizes and persists; the socket only pushes what happened. The only thing a client sends is a ping.
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final RealtimeSessionRegistry registry;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID userId = userIdOf(session);
        if (userId == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        registry.register(userId, session);
        // Tells the client it is live; it should refetch through REST whatever it may have missed while offline.
        registry.sendToSession(userId, session.getId(), RealtimeEnvelope.json(objectMapper, "connected", null));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        UUID userId = userIdOf(session);
        if (userId == null)
            return;
        try {
            JsonNode node = objectMapper.readTree(message.getPayload());
            if ("ping".equals(node.path("type").asText()))
                registry.sendToSession(userId, session.getId(), RealtimeEnvelope.json(objectMapper, "pong", null));
        } catch (Exception e) {
            // Unknown or malformed frames are ignored on purpose.
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.debug("WebSocket transport error on session {}: {}", session.getId(), exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UUID userId = userIdOf(session);
        if (userId != null)
            registry.unregister(userId, session.getId());
    }

    private UUID userIdOf(WebSocketSession session) {
        Object value = session.getAttributes().get(TicketHandshakeInterceptor.USER_ID_ATTRIBUTE);
        return value instanceof UUID id ? id : null;
    }
}
