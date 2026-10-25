package com.collectto.api_collectto.application.usecases.downloads;

import com.collectto.api_collectto.domain.ports.ApkDownloadRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class GetApkDownloadStatsUseCase {

    private final ApkDownloadRepository apkDownloadRepository;

    public long execute(String platform) {
        String resolvedPlatform = (platform == null || platform.isBlank()) ? "android" : platform.trim().toLowerCase();
        return apkDownloadRepository.countByPlatform(resolvedPlatform);
    }
}
