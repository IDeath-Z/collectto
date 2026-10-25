package com.collectto.api_collectto.application.usecases.downloads;

import java.time.Duration;
import java.time.Instant;

import com.collectto.api_collectto.domain.entities.ApkDownload;
import com.collectto.api_collectto.domain.ports.ApkDownloadRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class TrackApkDownloadUseCase {

    private static final Duration IP_COOLDOWN_WINDOW = Duration.ofMinutes(15);

    private final ApkDownloadRepository apkDownloadRepository;

    public record Input(String platform, String ipAddress, String userAgent) {}

    public void execute(Input input) {
        String resolvedPlatform = (input.platform() == null || input.platform().isBlank())
            ? "android"
            : input.platform().trim().toLowerCase();

        // Anti-Spam: Se o mesmo IP já registrou download nos últimos 15 minutos, ignora o incremento
        if (input.ipAddress() != null && !input.ipAddress().isBlank()) {
            Instant cooldownThreshold = Instant.now().minus(IP_COOLDOWN_WINDOW);
            boolean alreadyDownloadedRecently = apkDownloadRepository.existsRecentDownloadByIp(
                input.ipAddress(),
                resolvedPlatform,
                cooldownThreshold
            );

            if (alreadyDownloadedRecently) {
                log.info("Download ignorado no contador (cooldown anti-spam de 15min para IP: {})", input.ipAddress());
                return;
            }
        }

        ApkDownload download = ApkDownload.create(
            resolvedPlatform,
            input.ipAddress(),
            input.userAgent()
        );

        apkDownloadRepository.save(download);
    }
}
