package com.collectto.api_collectto.presentation.controllers;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.collectto.api_collectto.application.usecases.downloads.GetApkDownloadStatsUseCase;
import com.collectto.api_collectto.application.usecases.downloads.TrackApkDownloadUseCase;
import com.collectto.api_collectto.infrastructure.persistence.shared.TransactionalProxy;
import com.collectto.api_collectto.presentation.dto.download.ApkDownloadStatsResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/downloads")
public final class DownloadController {

    private final TransactionalProxy transactionalProxy;
    private final TrackApkDownloadUseCase trackApkDownloadUseCase;
    private final GetApkDownloadStatsUseCase getApkDownloadStatsUseCase;

    @Value("${app.apk-download-url:https://collectto.app/downloads/collectto.apk}")
    private String apkDownloadUrl;

    @GetMapping("/apk")
    public ResponseEntity<Void> downloadApk(
            @RequestParam(name = "platform", defaultValue = "android") String platform,
            HttpServletRequest request) {

        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);

        try {
            transactionalProxy.execute(() -> {
                trackApkDownloadUseCase.execute(new TrackApkDownloadUseCase.Input(
                    platform,
                    clientIp,
                    userAgent
                ));
            });
        } catch (Exception ex) {
            log.warn("Failed to record APK download metric: {}", ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(apkDownloadUrl))
            .build();
    }

    @GetMapping("/apk/count")
    public ResponseEntity<ApkDownloadStatsResponse> getApkDownloadCount(
            @RequestParam(name = "platform", defaultValue = "android") String platform) {

        long count = getApkDownloadStatsUseCase.execute(platform);
        return ResponseEntity.ok(new ApkDownloadStatsResponse(count));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
