package com.collectto.api_collectto.infrastructure.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

class RealtimeSessionRegistryTest {

    private final RealtimeSessionRegistry registry = new RealtimeSessionRegistry();

    private static WebSocketSession openSession(String id) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(id);
        when(session.isOpen()).thenReturn(true);
        return session;
    }

    @Test
    void deliversToEveryDeviceOfTheUserAndNobodyElse() throws Exception {
        UUID ana = UUID.randomUUID();
        UUID bruno = UUID.randomUUID();
        WebSocketSession phone = openSession("phone");
        WebSocketSession web = openSession("web");
        WebSocketSession brunoPhone = openSession("bruno");
        registry.register(ana, phone);
        registry.register(ana, web);
        registry.register(bruno, brunoPhone);

        registry.sendToUser(ana, "{\"type\":\"x\"}");

        verify(phone).sendMessage(new TextMessage("{\"type\":\"x\"}"));
        verify(web).sendMessage(new TextMessage("{\"type\":\"x\"}"));
        verify(brunoPhone, never()).sendMessage(any());
    }

    @Test
    void brokenSessionIsDroppedAndTheOthersKeepWorking() throws Exception {
        UUID ana = UUID.randomUUID();
        WebSocketSession broken = openSession("broken");
        WebSocketSession healthy = openSession("healthy");
        doThrow(new IOException("connection reset")).when(broken).sendMessage(any(WebSocketMessage.class));
        registry.register(ana, broken);
        registry.register(ana, healthy);

        registry.sendToUser(ana, "{}");

        assertThat(registry.sessionCount(ana)).isEqualTo(1);
        verify(healthy).sendMessage(new TextMessage("{}"));
    }

    @Test
    void oldestConnectionIsClosedAboveTheLimit() throws Exception {
        UUID ana = UUID.randomUUID();
        WebSocketSession first = openSession("s0");
        registry.register(ana, first);
        for (int i = 1; i <= RealtimeSessionRegistry.MAX_SESSIONS_PER_USER; i++)
            registry.register(ana, openSession("s" + i));

        assertThat(registry.sessionCount(ana)).isEqualTo(RealtimeSessionRegistry.MAX_SESSIONS_PER_USER);
        verify(first).close(any(CloseStatus.class));
    }

    @Test
    void userGoesOfflineWhenTheLastSessionCloses() {
        UUID ana = UUID.randomUUID();
        registry.register(ana, openSession("only"));
        assertThat(registry.isOnline(ana)).isTrue();

        registry.unregister(ana, "only");

        assertThat(registry.isOnline(ana)).isFalse();
    }
}
