package com.collectto.api_collectto.infrastructure.realtime;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;

// Authenticates the handshake: GET /ws?ticket=... ; no valid ticket, no connection (HTTP 401).
@Component
@RequiredArgsConstructor
public class TicketHandshakeInterceptor implements HandshakeInterceptor {

    static final String USER_ID_ATTRIBUTE = "userId";

    private final WebSocketTicketService ticketService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String ticket = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("ticket");
        Optional<UUID> userId = ticketService.consume(ticket);
        if (userId.isEmpty()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        attributes.put(USER_ID_ATTRIBUTE, userId.get());
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        // nothing to do
    }
}
