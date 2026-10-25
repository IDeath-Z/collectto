package com.collectto.api_collectto.infrastructure.persistence.notification;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.collectto.api_collectto.domain.entities.Notification;
import com.collectto.api_collectto.domain.enums.NotificationContext;
import com.collectto.api_collectto.domain.ports.NotificationRepository;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;
import com.collectto.api_collectto.domain.shared.DomainPageResult;
import com.collectto.api_collectto.infrastructure.persistence.shared.PageConverter;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public final class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationMapper mapper;

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Notification> findByRecipientIdAndActorIdAndContext(UUID recipientId, UUID actorId, NotificationContext context) {
        return jpaRepository.findByRecipientIdAndActorIdAndContext(recipientId, actorId, context)
                .map(mapper::toDomain);
    }

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity jpaEntity = mapper.toJpa(notification);
        NotificationJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public DomainPageResult<Notification> findByRecipientId(UUID recipientId, DomainPageRequest pageRequest) {
        PageRequest springPage = PageConverter.toSpring(pageRequest);
        Page<NotificationJpaEntity> page = jpaRepository.findByRecipientId(recipientId, springPage);
        
        return PageConverter.toDomain(page, mapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByRecipientIdAndActorIdAndContext(UUID recipientId, UUID actorId, NotificationContext context) {
        jpaRepository.deleteByRecipientIdAndActorIdAndContext(recipientId, actorId, context);
    }

    @Override
    public void deleteByActorIdAndReferenceIdAndContext(UUID actorId, UUID referenceId, NotificationContext context) {
        jpaRepository.deleteByActorIdAndReferenceIdAndContext(actorId, referenceId, context);
    }

    @Override
    public void markAllAsReadByRecipientId(UUID recipientId) {
        jpaRepository.markAllAsReadByRecipientId(recipientId);
    }
}
