package com.collectto.api_collectto.infrastructure.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.collectto.api_collectto.domain.entities.Chat.ChatId;
import com.collectto.api_collectto.domain.ports.ChatRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

class PresenceServiceTest {

    private final RealtimeSessionRegistry registry = new RealtimeSessionRegistry();
    private final ChatRepository chatRepository = mock(ChatRepository.class);
    private final TaskScheduler scheduler = mock(TaskScheduler.class);
    private final ScheduledFuture<?> future = mock(ScheduledFuture.class);
    private final List<Runnable> scheduled = new ArrayList<>();
    private final PresenceService presence = new PresenceService(registry, chatRepository, new ObjectMapper(), scheduler);

    private final UUID ana = UUID.randomUUID();
    private final UUID bruno = UUID.randomUUID();
    private final UUID stranger = UUID.randomUUID();
    private final List<String> brunoInbox = new ArrayList<>();

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() throws Exception {
        when(scheduler.schedule(any(Runnable.class), any(Instant.class))).thenAnswer(invocation -> {
            scheduled.add(invocation.getArgument(0));
            return (ScheduledFuture) future;
        });
        ChatId chat = new ChatId(ana, bruno);
        when(chatRepository.existsById(chat.getLowerUserId(), chat.getHigherUserId())).thenReturn(true);
        when(chatRepository.findPartnerIds(ana)).thenReturn(List.of(bruno));
        connect(bruno, "bruno-phone", brunoInbox);
    }

    private WebSocketSession connect(UUID userId, String sessionId, List<String> inbox) throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(sessionId);
        when(session.isOpen()).thenReturn(true);
        if (inbox != null)
            doAnswer(invocation -> {
                WebSocketMessage<?> message = invocation.getArgument(0);
                if (message instanceof TextMessage text)
                    inbox.add(text.getPayload());
                return null;
            }).when(session).sendMessage(any());
        registry.register(userId, session);
        return session;
    }

    private void anaConnects(String sessionId) throws Exception {
        connect(ana, sessionId, null);
        presence.onConnected(ana);
    }

    private void anaDisconnects(String sessionId) {
        registry.unregister(ana, sessionId);
        presence.onDisconnected(ana);
    }

    @Test
    void partnerSeesOnlineOnceEvenWithTwoDevices() throws Exception {
        anaConnects("phone");
        anaConnects("web");

        assertThat(presence.isOnline(ana)).isTrue();
        assertThat(brunoInbox).containsExactly("{\"type\":\"presence\",\"data\":{\"userId\":\"" + ana + "\",\"online\":true}}");
    }

    @Test
    void goesOfflineOnlyAfterTheGracePeriodAndTheLastDevice() throws Exception {
        anaConnects("phone");
        anaConnects("web");
        anaDisconnects("phone");
        assertThat(scheduled).isEmpty(); // web still connected

        anaDisconnects("web");
        assertThat(presence.isOnline(ana)).isTrue(); // still inside the grace period
        scheduled.get(0).run();

        assertThat(presence.isOnline(ana)).isFalse();
        assertThat(brunoInbox).last().asString().contains("\"online\":false");
    }

    @Test
    void reconnectingInsideTheGracePeriodNeverShowsOffline() throws Exception {
        anaConnects("phone");
        anaDisconnects("phone");
        anaConnects("phone-again");

        verify(future).cancel(false);
        assertThat(brunoInbox).hasSize(1); // only the first "online"
        assertThat(presence.isOnline(ana)).isTrue();
    }

    @Test
    void typingLastsUntilTheMessageIsSent() throws Exception {
        anaConnects("phone");
        presence.typingStarted(ana, bruno);
        presence.typingStarted(ana, bruno); // repeated start is ignored

        assertThat(presence.isTyping(ana, bruno)).isTrue();
        assertThat(brunoInbox).filteredOn(m -> m.contains("\"typing\"")).hasSize(1);

        presence.messageSent(ana, bruno);

        assertThat(presence.isTyping(ana, bruno)).isFalse();
        assertThat(brunoInbox).last().asString().contains("\"typing\":false");
    }

    @Test
    void typingStopsWhenTheClientSaysSo() throws Exception {
        anaConnects("phone");
        presence.typingStarted(ana, bruno);
        presence.typingStopped(ana, bruno);
        presence.typingStopped(ana, bruno); // second stop: nothing to announce

        assertThat(presence.isTyping(ana, bruno)).isFalse();
        assertThat(brunoInbox).filteredOn(m -> m.contains("\"typing\":false")).hasSize(1);
    }

    @Test
    void typingEndsWhenTheTyperGoesOffline() throws Exception {
        anaConnects("phone");
        presence.typingStarted(ana, bruno);
        anaDisconnects("phone");
        scheduled.get(0).run();

        assertThat(presence.isTyping(ana, bruno)).isFalse();
        assertThat(brunoInbox).anyMatch(m -> m.contains("\"typing\":false"));
    }

    @Test
    void cannotTypeToSomeoneWithoutAChat() throws Exception {
        List<String> strangerInbox = new ArrayList<>();
        connect(stranger, "stranger", strangerInbox);
        anaConnects("phone");

        presence.typingStarted(ana, stranger);

        assertThat(presence.isTyping(ana, stranger)).isFalse();
        assertThat(strangerInbox).isEmpty();
        verify(chatRepository, never()).findPartnerIds(eq(stranger));
    }
}
