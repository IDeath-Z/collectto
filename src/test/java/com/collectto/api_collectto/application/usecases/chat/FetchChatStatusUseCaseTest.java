package com.collectto.api_collectto.application.usecases.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.collectto.api_collectto.domain.ports.ChatRepository;
import com.collectto.api_collectto.domain.ports.PresenceReader;

class FetchChatStatusUseCaseTest {

    private final ChatRepository chatRepository = mock(ChatRepository.class);
    private final PresenceReader presence = mock(PresenceReader.class);
    private final FetchChatStatusUseCase useCase = new FetchChatStatusUseCase(chatRepository, presence);
    private final UUID me = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();

    @Test
    void showsOnlineAndTypingOfTheOtherUserTowardsMe() {
        when(chatRepository.existsById(any(), any())).thenReturn(true);
        when(presence.isOnline(other)).thenReturn(true);
        when(presence.isTyping(other, me)).thenReturn(true);

        var output = useCase.execute(new FetchChatStatusUseCase.Input(me, other));

        assertThat(output.online()).isTrue();
        assertThat(output.typing()).isTrue();
    }

    @Test
    void strangersSeeNothing() {
        when(chatRepository.existsById(any(), any())).thenReturn(false);

        var output = useCase.execute(new FetchChatStatusUseCase.Input(me, other));

        assertThat(output.online()).isFalse();
        assertThat(output.typing()).isFalse();
        verifyNoInteractions(presence);
    }
}
