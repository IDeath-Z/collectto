package com.collectto.api_collectto.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.collectto.api_collectto.application.usecases.downloads.GetApkDownloadStatsUseCase;
import com.collectto.api_collectto.application.usecases.downloads.TrackApkDownloadUseCase;
import com.collectto.api_collectto.domain.ports.ApkDownloadRepository;

@Configuration
public class DownloadConfig {

    @Bean
    public TrackApkDownloadUseCase trackApkDownloadUseCase(ApkDownloadRepository apkDownloadRepository) {
        return new TrackApkDownloadUseCase(apkDownloadRepository);
    }

    @Bean
    public GetApkDownloadStatsUseCase getApkDownloadStatsUseCase(ApkDownloadRepository apkDownloadRepository) {
        return new GetApkDownloadStatsUseCase(apkDownloadRepository);
    }
}
