package com.collectto.api_collectto.presentation.controllers;

import java.util.UUID;

import com.collectto.api_collectto.application.usecases.user.UpdateUserUseCase;
import com.collectto.api_collectto.application.usecases.userfollow.AcceptFollowUseCase;
import com.collectto.api_collectto.application.usecases.userfollow.DeclineFollowUseCase;
import com.collectto.api_collectto.application.usecases.userfollow.FollowUserUseCase;
import com.collectto.api_collectto.application.usecases.userfollow.UnfollowUserUseCase;
import com.collectto.api_collectto.infrastructure.persistence.shared.TransactionalProxy;
import com.collectto.api_collectto.infrastructure.security.SecurityUserDetails;
import com.collectto.api_collectto.presentation.dto.user.UpdateUserRequest;
import com.collectto.api_collectto.presentation.dto.user.UserFollowResponse;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.collectto.api_collectto.application.usecases.user.ChangePasswordUseCase;
import com.collectto.api_collectto.application.usecases.user.CreateUserUseCase;
import com.collectto.api_collectto.application.usecases.user.DeleteUserUseCase;
import com.collectto.api_collectto.application.usecases.user.FetchCurrentUserInfoUseCase;
import com.collectto.api_collectto.application.usecases.user.FetchUserUseCase;
import com.collectto.api_collectto.presentation.dto.user.ChangePasswordRequest;
import com.collectto.api_collectto.presentation.dto.user.CreateUserRequest;
import com.collectto.api_collectto.presentation.dto.user.CreateUserResponse;
import com.collectto.api_collectto.presentation.dto.user.CurrentUserInfoResponse;
import com.collectto.api_collectto.presentation.dto.user.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public final class UserController {

    private final ChangePasswordUseCase changePasswordUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final FetchCurrentUserInfoUseCase fetchCurrentUserInfoUseCase;
    private final FetchUserUseCase fetchUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final AcceptFollowUseCase acceptFollowUseCase;
    private final DeclineFollowUseCase declineFollowUseCase;
    private final FollowUserUseCase followUserUseCase;
    private final UnfollowUserUseCase unfollowUserUseCase;
    private final TransactionalProxy transactionalProxy;

    @PatchMapping("/password")
    @Operation(summary = "Change user password", description = "Allows the authenticated user to change their password by providing the current password and a new password.")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal SecurityUserDetails userDetails, @RequestBody ChangePasswordRequest request) {
        UUID userId = userDetails.getUser().getId();

        transactionalProxy.execute(() -> changePasswordUseCase.execute(
            new ChangePasswordUseCase.Input(
                userId,
                request.currentPassword(),
                request.newPassword()
            )
        ));
        
        return ResponseEntity.noContent().build();
    }
    

    @PostMapping("/create")
    @Operation(summary = "Create a new user", description = "Registers a new user in the system with the provided details.")
    public ResponseEntity<CreateUserResponse> create(@RequestBody @Valid CreateUserRequest request) {
        var output = transactionalProxy.execute(() -> createUserUseCase.execute(
            new CreateUserUseCase.Input(
                request.name(),
                request.username(),
                request.email(),
                request.password(),
                request.birthdayDate()
            )
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CreateUserResponse(
                output.id(),
                output.name(),
                output.username(),
                output.email(),
                output.creationDate()
            ));
    }

    @GetMapping("/me")
    @Operation(summary = "Fetch authenticated user details", description = "Retrieves the details of the currently authenticated user.")
    public ResponseEntity<CurrentUserInfoResponse> getAuthenticatedUser(@AuthenticationPrincipal SecurityUserDetails userDetails) {
        UUID userId = userDetails.getUser().getId();

        var output = transactionalProxy.executeReadOnly(() -> fetchCurrentUserInfoUseCase.execute(userId));

        return ResponseEntity.ok(new CurrentUserInfoResponse(
            output.id(),
            output.name(),
            output.username(),
            output.email(),
            output.bio(),
            output.profilePictureUrl(),
            output.profileBackgroundUrl(),
            output.followersCount(),
            output.followingCount(),
            output.isActive(),
            output.birthdayDate(),
            output.creationDate()
        ));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Deactivate user account", description = "Deactivates the authenticated user's account.")
    public ResponseEntity<Void> deactivateAccount(@AuthenticationPrincipal SecurityUserDetails userDetails) {
        UUID userId = userDetails.getUser().getId();
    
        transactionalProxy.execute(() -> deleteUserUseCase.execute(userId));
        
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "Fetch user details", description = "Retrieves the details of a user by their unique identifier.")
    public ResponseEntity<UserResponse> get(@PathVariable UUID userId) {
        var output = transactionalProxy.executeReadOnly(() -> fetchUserUseCase.execute(new FetchUserUseCase.Input(userId)));

        return ResponseEntity.ok(new UserResponse(
            output.id(),
            output.name(),
            output.username(),
            output.bio(),
            output.profilePictureUrl(),
            output.profileBackgroundUrl(),
            output.followersCount(),
            output.followingCount(),
            output.creationDate()
        ));
    }

    @PatchMapping("/update")
    @Operation(summary = "Update user profile", description = "Updates the profile of the authenticated user. Only the provided fields will be updated.")
    public ResponseEntity<CurrentUserInfoResponse> patch(@AuthenticationPrincipal SecurityUserDetails userDetails, @RequestBody @Valid UpdateUserRequest request) {
        UUID userId = userDetails.getUser().getId();

        var output = transactionalProxy.execute(() -> updateUserUseCase.execute(
            new UpdateUserUseCase.Input(
                userId,
                request.name(),
                request.username(),
                request.bio(),
                request.profilePictureUrl(),
                request.profileBackgroundUrl(),
                request.birthdayDate()
            )
        ));

        return ResponseEntity.ok(new CurrentUserInfoResponse(
            output.id(),
            output.name(),
            output.username(),
            output.email(),
            output.bio(),
            output.profilePictureUrl(),
            output.profileBackgroundUrl(),
            output.followersCount(),
            output.followingCount(),
            output.isActive(),
            output.birthdayDate(),
            output.creationDate()
        ));
    }

    // Follow
    @PatchMapping("/follow/{followerId}/accept")
    @Operation(summary = "Accept a follow request", description = "Accepts a follow request from the specified user.")
    public ResponseEntity<UserFollowResponse> acceptFollowRequest(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID followerId) {
        UUID userId = userDetails.getUser().getId();

        var output = transactionalProxy.execute(() -> acceptFollowUseCase.execute(new AcceptFollowUseCase.Input(followerId, userId)));
        
        return ResponseEntity.ok(new UserFollowResponse(
            output.followerId(),
            output.followedId(),
            output.status(),
            output.createdAt()
        ));
    }

    @PatchMapping("/follow/{followerId}/decline")
    @Operation(summary = "Decline a follow request", description = "Declines a follow request from the specified user.")
    public ResponseEntity<UserFollowResponse> declineFollowRequest(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID followerId) {
        UUID userId = userDetails.getUser().getId();

        var output = transactionalProxy.execute(() -> declineFollowUseCase.execute(new DeclineFollowUseCase.Input(followerId, userId)));
        
        return ResponseEntity.ok(new UserFollowResponse(
            output.followerId(),
            output.followedId(),
            output.status(),
            output.createdAt()
        ));
    }

    @PostMapping("/follow/{followedId}")
    @Operation(summary = "Follow a user", description = "Sends a follow request to the specified user. If the request is successful, the follow status will be PENDING until accepted by the followed user.")
    public ResponseEntity<UserFollowResponse> followUser(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID followedId) {
        UUID userId = userDetails.getUser().getId();

        var output = transactionalProxy.execute(() -> followUserUseCase.execute(
            new FollowUserUseCase.Input(userId, followedId)
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new UserFollowResponse(
                output.followerId(), 
                output.followedId(), 
                output.status(), 
                output.createdAt()
            ));
    }

    @DeleteMapping("/follow/{followedId}")
    @Operation(summary = "Unfollow a user", description = "Removes the follow relationship with the specified user.")
    public ResponseEntity<Void> unfollowUser(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID followedId) {
        UUID userId = userDetails.getUser().getId();

        transactionalProxy.execute(() -> unfollowUserUseCase.execute(
            new UnfollowUserUseCase.Input(userId, followedId)
        ));

        return ResponseEntity.noContent().build();
    }
}
