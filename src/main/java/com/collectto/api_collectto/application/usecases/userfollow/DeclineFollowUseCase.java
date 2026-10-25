package com.collectto.api_collectto.application.usecases.userfollow;

import java.time.Instant;
import java.util.UUID;

import com.collectto.api_collectto.application.exceptions.BusinessRuleException;
import com.collectto.api_collectto.application.exceptions.ResourceNotFoundException;
import com.collectto.api_collectto.domain.entities.UserFollow;
import com.collectto.api_collectto.domain.enums.FollowStatus;
import com.collectto.api_collectto.domain.enums.NotificationContext;
import com.collectto.api_collectto.domain.ports.NotificationRepository;
import com.collectto.api_collectto.domain.ports.UserFollowRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DeclineFollowUseCase {

    private final UserFollowRepository userFollowRepository;
    private final NotificationRepository notificationRepository;

    public record Input(UUID followerId, UUID followedId) {}
    public record Output(UUID followerId, UUID followedId, FollowStatus status, Instant createdAt) {}

    public Output execute(Input input) {
        if (input.followerId().equals(input.followedId()))
            throw new BusinessRuleException("Follower and followed users must be different.");

        UserFollow followRequest = userFollowRepository.findById(input.followerId(), input.followedId())
            .orElseThrow(() -> new ResourceNotFoundException("Follow request not found with followerId: " + input.followerId() + " and followedId: " + input.followedId()));

        UserFollow declinedFollow = userFollowRepository.save(followRequest.decline());
        notificationRepository.deleteByRecipientIdAndActorIdAndContext(
            followRequest.getFollowedId(), 
            followRequest.getFollowerId(), 
            NotificationContext.USER_FOLLOW_REQUESTED
        );

        return new Output(
            declinedFollow.getFollowerId(),
            declinedFollow.getFollowedId(),
            declinedFollow.getStatus(),
            declinedFollow.getCreatedAt()
        );
    }
}
