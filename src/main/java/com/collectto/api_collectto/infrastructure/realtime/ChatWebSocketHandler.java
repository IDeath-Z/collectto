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

// Writes that persist (send message, mark as read) stay in REST, which already validates, authorizes and saves.
// The socket pushes what happened and receives only ephemeral signals: ping, typing.start, typing.stop.
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final RealtimeSessionRegistry registry;
    private final PresenceService presenceService;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID userId = userIdOf(session);
        if (userId == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        registry.register(userId, session);
        presenceService.onConnected(userId);
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
            switch (node.path("type").asText()) {
                case "ping" -> registry.sendToSession(userId, session.getId(), RealtimeEnvelope.json(objectMapper, "pong", null));
                case "typing.start" -> presenceService.typingStarted(userId, UUID.fromString(node.path("to").asText()));
                case "typing.stop" -> presenceService.typingStopped(userId, UUID.fromString(node.path("to").asText()));
                default -> { /* unknown frame: ignored */ }
            }
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
        if (userId != null) {
            registry.unregister(userId, session.getId());
            presenceService.onDisconnected(userId);
        }
    }

    private UUID userIdOf(WebSocketSession session) {
        Object value = session.getAttributes().get(TicketHandshakeInterceptor.USER_ID_ATTRIBUTE);
        return value instanceof UUID id ? id : null;
    }
}
