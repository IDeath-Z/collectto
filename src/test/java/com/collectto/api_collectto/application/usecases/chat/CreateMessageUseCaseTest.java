package com.collectto.api_collectto.application.usecases.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.collectto.api_collectto.domain.entities.Chat;
import com.collectto.api_collectto.domain.entities.Message;
import com.collectto.api_collectto.domain.ports.ChatEventPublisher;
import com.collectto.api_collectto.domain.ports.ChatRepository;
import com.collectto.api_collectto.domain.ports.MessageRepository;

class CreateMessageUseCaseTest {

    @Test
    void savesTheMessageAndAnnouncesItToTheRecipient() {
        ChatRepository chatRepository = mock(ChatRepository.class);
        MessageRepository messageRepository = mock(MessageRepository.class);
        ChatEventPublisher publisher = mock(ChatEventPublisher.class);
        when(chatRepository.findById(any(), any())).thenReturn(Optional.empty());
        when(chatRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, Chat.class));

        UUID sender = UUID.randomUUID();
        UUID receiver = UUID.randomUUID();
        var output = new CreateMessageUseCase(chatRepository, messageRepository, publisher)
            .execute(new CreateMessageUseCase.Input(sender, receiver, "Oi, ainda tem o LP?"));

        ArgumentCaptor<Message> saved = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(saved.capture());
        verify(publisher).messageCreated(saved.getValue(), receiver);
        assertThat(output.id()).isEqualTo(saved.getValue().getId());
        assertThat(saved.getValue().getSenderId()).isEqualTo(sender);
        assertThat(saved.getValue().getReadAt()).isNull();
    }
}
