package com.collectto.api_collectto.application.usecases.downloads;

import com.collectto.api_collectto.domain.entities.ApkDownload;
import com.collectto.api_collectto.domain.ports.ApkDownloadRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TrackApkDownloadUseCase {

    private final ApkDownloadRepository apkDownloadRepository;

    public record Input(String platform, String ipAddress, String userAgent) {}

    public ApkDownload execute(Input input) {
        ApkDownload download = ApkDownload.create(
            input.platform(),
            input.ipAddress(),
            input.userAgent()
        );

        return apkDownloadRepository.save(download);
    }
}
