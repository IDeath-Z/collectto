package com.collectto.api_collectto.application.usecases.user;

import com.collectto.api_collectto.application.exceptions.ResourceNotFoundException;
import com.collectto.api_collectto.domain.entities.User;
import com.collectto.api_collectto.domain.enums.FollowState;
import com.collectto.api_collectto.domain.ports.UserFollowRepository;
import com.collectto.api_collectto.domain.ports.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public final class FetchUserUseCase {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;

    public record Input(UUID requesterId, UUID userId) {}
    public record Output(UUID id, String name, String username, String bio, String profilePictureUrl,
        String profileBackgroundUrl, int followersCount, int followingCount, FollowState followState, Instant creationDate) {}

    public Output execute(Input input) {
        User user = userRepository.findById(input.userId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + input.userId()));

        FollowState followState = input.requesterId().equals(input.userId()) ? FollowState.NONE :
            userFollowRepository.findById(input.requesterId(), input.userId())
                .map(follow -> FollowState.fromStatus(follow.getStatus()))
                .orElse(FollowState.NONE);

        return new Output(
            user.getId(),
            user.getName(),
            user.getUsername(),
            user.getBio(),
            user.getProfilePictureUrl(),
            user.getProfileBackgroundUrl(),
            user.getFollowersCount(),
            user.getFollowingCount(),
            followState,
            user.getCreationDate()
        );
    }
}
