package com.collectto.api_collectto.infrastructure.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import com.collectto.api_collectto.infrastructure.realtime.ChatWebSocketHandler;
import com.collectto.api_collectto.infrastructure.realtime.TicketHandshakeInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    public static final String WEBSOCKET_PATH = "/ws";

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final TicketHandshakeInterceptor ticketHandshakeInterceptor;
    private final CorsProperties corsProperties;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Same origins as REST CORS. The native app sends no Origin header, which Spring accepts.
        List<String> origins = corsProperties.allowedOriginPatterns() == null ? List.of() : corsProperties.allowedOriginPatterns();
        registry.addHandler(chatWebSocketHandler, WEBSOCKET_PATH)
            .addInterceptors(ticketHandshakeInterceptor)
            .setAllowedOriginPatterns(origins.toArray(String[]::new));
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8 * 1024); // clients only send pings
        container.setMaxBinaryMessageBufferSize(1024);
        container.setMaxSessionIdleTimeout(90_000L);     // server pings every 25 s, so only dead sessions hit this
        return container;
    }
}
