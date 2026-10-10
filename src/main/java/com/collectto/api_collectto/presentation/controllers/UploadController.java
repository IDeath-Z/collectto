package com.collectto.api_collectto.presentation.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collectto.api_collectto.application.usecases.storage.GenerateUploadUrlsUseCase;
import com.collectto.api_collectto.infrastructure.security.SecurityUserDetails;
import com.collectto.api_collectto.presentation.dto.storage.GenerateUploadUrlsRequest;
import com.collectto.api_collectto.presentation.dto.storage.GenerateUploadUrlsResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/uploads")
public final class UploadController {

    private final GenerateUploadUrlsUseCase generateUploadUrlsUseCase;

    @PostMapping("/presigned-urls")
    @Operation(summary = "Generate pre-signed upload URLs", description = "Returns pre-signed URLs for direct upload to storage. Valid for 5 minutes.")
    public ResponseEntity<GenerateUploadUrlsResponse> generatePresignedUrls(@AuthenticationPrincipal SecurityUserDetails userDetails, @RequestBody @Valid GenerateUploadUrlsRequest request) {
        UUID userId = userDetails.getUser().getId();

        var output = generateUploadUrlsUseCase.execute(new GenerateUploadUrlsUseCase.Input(
            userId,
            request.resourceId(),
            request.context(),
            request.files().stream()
                .map(file -> new GenerateUploadUrlsUseCase.FileInput(file.fileName(), file.contentType()))
                .toList()
        ));

        return ResponseEntity.ok(new GenerateUploadUrlsResponse(
            output.resourceId(),
            output.files().stream()
                .map(file -> new GenerateUploadUrlsResponse.FileOutput(file.filePath(), file.uploadUrl()))
                .toList()
        ));
    }
}
