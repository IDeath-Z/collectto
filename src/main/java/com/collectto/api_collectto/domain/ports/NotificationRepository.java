package com.collectto.api_collectto.domain.ports;

import java.util.Optional;
import java.util.UUID;

import com.collectto.api_collectto.domain.entities.Notification;
import com.collectto.api_collectto.domain.enums.NotificationContext;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;
import com.collectto.api_collectto.domain.shared.DomainPageResult;

public interface NotificationRepository {

    Optional<Notification> findById(UUID id);
    Optional<Notification> findByRecipientIdAndActorIdAndContext(UUID recipientId, UUID actorId, NotificationContext context);
    Notification save(Notification notification);
    DomainPageResult<Notification> findByRecipientId(UUID recipientId, DomainPageRequest pageRequest);
    boolean existsById(UUID id);
    void deleteById(UUID id);
    void deleteByRecipientIdAndActorIdAndContext(UUID recipientId, UUID actorId, NotificationContext context);
    void deleteByActorIdAndReferenceIdAndContext(UUID actorId, UUID referenceId, NotificationContext context);
    void markAllAsReadByRecipientId(UUID recipientId);
}
