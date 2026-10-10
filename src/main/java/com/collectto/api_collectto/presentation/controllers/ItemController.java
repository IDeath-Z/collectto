package com.collectto.api_collectto.presentation.controllers;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collectto.api_collectto.application.usecases.item.CreateItemUseCase;
import com.collectto.api_collectto.application.usecases.item.DeleteItemUseCase;
import com.collectto.api_collectto.application.usecases.item.FetchCollectionItemsUseCase;
import com.collectto.api_collectto.application.usecases.item.FetchItemUseCase;
import com.collectto.api_collectto.application.usecases.item.UpdateItemUseCase;
import com.collectto.api_collectto.application.usecases.itemcomment.CommentItemUseCase;
import com.collectto.api_collectto.application.usecases.itemcomment.DeleteCommentUseCase;
import com.collectto.api_collectto.application.usecases.itemcomment.FetchItemCommentsUseCase;
import com.collectto.api_collectto.application.usecases.itemlike.FetchItemLikesUseCase;
import com.collectto.api_collectto.application.usecases.itemlike.LikeItemUseCase;
import com.collectto.api_collectto.application.usecases.itemlike.UnlikeItemUseCase;
import com.collectto.api_collectto.domain.enums.SortBy;
import com.collectto.api_collectto.domain.shared.DomainPageRequest;
import com.collectto.api_collectto.infrastructure.persistence.shared.TransactionalProxy;
import com.collectto.api_collectto.infrastructure.security.SecurityUserDetails;
import com.collectto.api_collectto.presentation.dto.item.CreateCommentRequest;
import com.collectto.api_collectto.presentation.dto.item.CreateCommentResponse;
import com.collectto.api_collectto.presentation.dto.item.CreateItemRequest;
import com.collectto.api_collectto.presentation.dto.item.ItemCommentPageResponse;
import com.collectto.api_collectto.presentation.dto.item.ItemLikeResponse;
import com.collectto.api_collectto.presentation.dto.item.ItemLikesPageResponse;
import com.collectto.api_collectto.presentation.dto.item.ItemPageResponse;
import com.collectto.api_collectto.presentation.dto.item.ItemResponse;
import com.collectto.api_collectto.presentation.dto.item.UpdateItemRequest;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public final class ItemController {

    private final CreateItemUseCase createItemUseCase;
    private final DeleteItemUseCase deleteItemUseCase;
    private final FetchCollectionItemsUseCase fetchCollectionItemsUseCase;
    private final FetchItemUseCase fetchItemUseCase;
    private final UpdateItemUseCase updateItemUseCase;
    private final LikeItemUseCase likeItemUseCase;
    private final UnlikeItemUseCase unlikeItemUseCase;
    private final FetchItemLikesUseCase fetchItemLikesUseCase;
    private final CommentItemUseCase commentItemUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;
    private final FetchItemCommentsUseCase fetchItemCommentsUseCase;
    private final TransactionalProxy transactionalProxy;

    @PostMapping(value = "/create")
    @Operation(summary = "Create a new item", description = "Registers a new item in the system with the provided details.")
    public ResponseEntity<ItemResponse> create(@AuthenticationPrincipal SecurityUserDetails userDetails, @RequestBody @Valid CreateItemRequest request) {
        UUID userId = userDetails.getUser().getId();

        var output = transactionalProxy.execute(() ->  createItemUseCase.execute(
            new CreateItemUseCase.Input(
                request.collectionId(),
                userId,
                request.name(),
                request.description(),
                request.acquisitionDate(),
                request.lastUsedDate(),
                request.imageFilesUrls(),
                request.attributes(),
                request.tags()
            )
        ));
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ItemResponse(
                output.id(),
                output.collectionId(),
                output.userId(),
                output.name(),
                output.description(),
                output.acquisitionDate(),
                output.lastUsedDate(),
                output.imageFilesUrls(),
                output.attributes(),
                output.likesCount(),
                output.commentsCount(),
                output.tags(),
                output.isActive(),
                output.createdAt(),
                output.updatedAt()
            ));
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Delete an item", description = "Deletes an item by its ID. Only the owner of the item can perform this action.")
    public ResponseEntity<Void> deleteItem(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID itemId) {
        UUID userId = userDetails.getUser().getId();

        transactionalProxy.execute(() -> deleteItemUseCase.execute(
            new DeleteItemUseCase.Input(itemId, userId)
        ));

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-collection/{collectionId}")
    @Operation(summary = "Get paginated items by collection", description = "Retrieves a paginated list of items belonging to a specific collection, with optional sorting.")
    public ResponseEntity<ItemPageResponse> getPaginatedItems(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID collectionId, 
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "CREATED_AT_DESC") SortBy sortBy) {

        UUID requesterId = userDetails.getUser().getId();

        var output = transactionalProxy.executeReadOnly(() -> fetchCollectionItemsUseCase.execute(
            new FetchCollectionItemsUseCase.Input(
                collectionId, 
                requesterId, 
                new DomainPageRequest(page, size, sortBy)
            )
        ));

        return ResponseEntity.ok(new ItemPageResponse(
            output.items().stream()
                .map(item -> new ItemPageResponse.ItemSummaryResponse(
                    item.id(),
                    item.name(),
                    item.imagesURL()
                ))
                .toList(),
            output.totalPages(),
            output.totalItems(),
            output.currentPage()
        ));
    }

    @GetMapping("/{collectionId}/{itemId}")
    @Operation(summary = "Fetch item details", description = "Retrieves detailed information about a specific item, including its attributes and media URLs.")
    public ResponseEntity<ItemResponse> getItemById(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID collectionId, @PathVariable UUID itemId) {
        UUID requesterId = userDetails.getUser().getId();

        var output = transactionalProxy.executeReadOnly(() -> fetchItemUseCase.execute(
            new FetchItemUseCase.Input(
                itemId,
                collectionId,
                requesterId
            )
        ));

        return ResponseEntity.ok(new ItemResponse(
            output.id(),
            output.collectionId(),
            output.userId(),
            output.name(),
            output.description(),
            output.acquisitionDate(),
            output.lastUsedDate(),
            output.imageFilesUrls(),
            output.attributes(),
            output.likesCount(),
            output.commentsCount(),
            output.tags(),
            output.isActive(),
            output.createdAt(),
            output.updatedAt()
        ));
    }

    @PatchMapping("/update/{itemId}")
    @Operation(summary = "Update an existing item", description = "Updates the details of an existing item. Only the details provided in the request will be updated.")
    public ResponseEntity<ItemResponse> update(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID itemId, @RequestBody @Valid UpdateItemRequest request) {
        UUID userId = userDetails.getUser().getId();

        var output = transactionalProxy.execute(() -> updateItemUseCase.execute(
            new UpdateItemUseCase.Input(
                itemId,
                userId,
                request.collectionId(),
                request.name(),
                request.description(),
                request.acquisitionDate(),
                request.imageFilesUrls(),
                request.attributes(),
                request.tags()
            )
        ));

       return ResponseEntity.ok(new ItemResponse(
            output.id(),
            output.collectionId(),
            output.userId(),
            output.name(),
            output.description(),
            output.acquisitionDate(),
            output.lastUsedDate(),
            output.imageFilesUrls(),
            output.attributes(),
            output.likesCount(),
            output.commentsCount(),
            output.tags(),
            output.isActive(),
            output.createdAt(),
            output.updatedAt()
        ));
    }

    // Likes
    @PostMapping("/like/{itemId}")
    @Operation(summary = "Like an item", description = "Allows a user to like an item. If the user has already liked the item, an error will be returned.")
    public ResponseEntity<ItemLikeResponse> likeItem(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID itemId) {
        UUID userId = userDetails.getUser().getId();

        var output = transactionalProxy.execute(() -> likeItemUseCase.execute(
            new LikeItemUseCase.Input(
                itemId,
                userId
            )
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ItemLikeResponse(
                output.itemId(),
                output.likerId(),
                output.createdAt()
            ));
    }

    @DeleteMapping("/like/{itemId}")
    @Operation(summary = "Unlike an item", description = "Allows a user to unlike an item")
    public ResponseEntity<Void> unlikeItem(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID itemId) {
        UUID userId = userDetails.getUser().getId();

        transactionalProxy.execute(() -> unlikeItemUseCase.execute(
            new UnlikeItemUseCase.Input(itemId, userId)
        ));

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/likes/{itemId}")
    @Operation(summary = "Fetch item likes", description = "Retrieves a paginated list of users who liked a specific item, including their basic information and profile picture URLs.")
    public ResponseEntity<ItemLikesPageResponse> getItemLikes(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID itemId, 
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "CREATED_AT_DESC") SortBy sortBy) {
        UUID userId = userDetails.getUser().getId();

        var output = transactionalProxy.executeReadOnly(() -> fetchItemLikesUseCase.execute(
            new FetchItemLikesUseCase.Input(
                itemId, 
                userId, 
                new DomainPageRequest(page, size, sortBy)
            )
        ));

        return ResponseEntity.ok(new ItemLikesPageResponse(
            output.likers().stream()
                .map(liker -> new ItemLikesPageResponse.LikerSummaryResponse(
                    liker.userId(),
                    liker.name(),
                    liker.username(),
                    liker.profilePictureURL()
                ))
                .toList(),
            output.totalPages(),
            output.totalElements(),
            output.currentPage()
        ));
    }
    
    // Comments
    @PostMapping("/comment/{itemId}")
    @Operation(summary = "Comment on an item", description = "Allows a user to comment on an item. The comment will be added to the item and associated with the user.")
    public ResponseEntity<CreateCommentResponse> commentOnItem(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID itemId, @RequestBody @Valid CreateCommentRequest request) {
        UUID userId = userDetails.getUser().getId();

        var output = transactionalProxy.execute(() -> commentItemUseCase.execute(
            new CommentItemUseCase.Input(
                itemId,
                userId,
                request.content()
            )
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CreateCommentResponse(
                output.commentId(),
                output.itemId(),
                output.authorId(),
                output.content(),
                output.createdAt()
            ));
    }

    @DeleteMapping("/comment/{commentId}")
    @Operation(summary = "Delete a comment", description = "Allows a user to delete their own comment or comments on their item.")
    public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID commentId) {
        UUID userId = userDetails.getUser().getId();

        transactionalProxy.execute(() -> deleteCommentUseCase.execute(
            new DeleteCommentUseCase.Input(commentId, userId)
        ));

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comments/{itemId}")
    @Operation(summary = "Fetch item comments", description = "Retrieves a paginated list of comments for a specific item, including the comment content, author information, and timestamps.")
    public ResponseEntity<ItemCommentPageResponse> getItemComments(@AuthenticationPrincipal SecurityUserDetails userDetails, @PathVariable UUID itemId, 
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "CREATED_AT_DESC") SortBy sortBy) {
        UUID userId = userDetails.getUser().getId();

        var output = transactionalProxy.executeReadOnly(() -> fetchItemCommentsUseCase.execute(
            new FetchItemCommentsUseCase.Input(
                itemId, 
                userId, 
                new DomainPageRequest(page, size, sortBy)
            )
        ));

        return ResponseEntity.ok(new ItemCommentPageResponse(
            output.commenterSummaries().stream()
                .map(commenter -> new ItemCommentPageResponse.CommenterSummaryResponse(
                    commenter.commentId(),
                    commenter.userId(),
                    commenter.username(),
                    commenter.profilePictureURL(),
                    commenter.content(),
                    commenter.createdAt()
                ))
                .toList(),
            output.totalPages(),
            output.totalElements(),
            output.currentPage()
        ));
    } 
}