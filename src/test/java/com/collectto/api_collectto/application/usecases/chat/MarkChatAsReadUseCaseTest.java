package com.collectto.api_collectto.application.usecases.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.collectto.api_collectto.application.exceptions.ResourceNotFoundException;
import com.collectto.api_collectto.domain.entities.Chat.ChatId;
import com.collectto.api_collectto.domain.ports.ChatEventPublisher;
import com.collectto.api_collectto.domain.ports.ChatRepository;
import com.collectto.api_collectto.domain.ports.MessageRepository;

class MarkChatAsReadUseCaseTest {

    private final ChatRepository chatRepository = mock(ChatRepository.class);
    private final MessageRepository messageRepository = mock(MessageRepository.class);
    private final ChatEventPublisher publisher = mock(ChatEventPublisher.class);
    private final MarkChatAsReadUseCase useCase = new MarkChatAsReadUseCase(chatRepository, messageRepository, publisher);

    private final UUID requester = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();
    private final ChatId chatId = new ChatId(requester, other);

    @Test
    void marksOnlyMessagesSentByTheOtherUserAndAnnouncesTheReadReceipt() {
        when(chatRepository.existsById(chatId.getLowerUserId(), chatId.getHigherUserId())).thenReturn(true);
        when(messageRepository.markAllAsReadFromSender(any(), any(), any(), any())).thenReturn(3);

        Instant before = Instant.now();
        useCase.execute(new MarkChatAsReadUseCase.Input(requester, other));

        ArgumentCaptor<Instant> readAt = ArgumentCaptor.forClass(Instant.class);
        // sender filter is the OTHER user: the requester never marks their own messages
        verify(messageRepository).markAllAsReadFromSender(
            eq(chatId.getLowerUserId()), eq(chatId.getHigherUserId()), eq(other), readAt.capture());
        assertThat(readAt.getValue()).isAfterOrEqualTo(before);
        verify(publisher).messagesRead(requester, other, readAt.getValue());
    }

    @Test
    void staysSilentWhenThereWasNothingToMark() {
        when(chatRepository.existsById(chatId.getLowerUserId(), chatId.getHigherUserId())).thenReturn(true);
        when(messageRepository.markAllAsReadFromSender(any(), any(), any(), any())).thenReturn(0);

        useCase.execute(new MarkChatAsReadUseCase.Input(requester, other));

        verifyNoInteractions(publisher);
    }

    @Test
    void throwsWhenChatDoesNotExistAndTouchesNothing() {
        when(chatRepository.existsById(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(new MarkChatAsReadUseCase.Input(requester, other)))
            .isInstanceOf(ResourceNotFoundException.class);
        verify(messageRepository, never()).markAllAsReadFromSender(any(), any(), any(), any());
        verifyNoInteractions(publisher);
    }

    @Test
    void rejectsChatWithYourself() {
        assertThatThrownBy(() -> useCase.execute(new MarkChatAsReadUseCase.Input(requester, requester)))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
