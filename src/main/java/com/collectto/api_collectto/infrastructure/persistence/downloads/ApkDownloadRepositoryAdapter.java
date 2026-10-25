package com.collectto.api_collectto.infrastructure.persistence.downloads;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.collectto.api_collectto.domain.entities.ApkDownload;
import com.collectto.api_collectto.domain.ports.ApkDownloadRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApkDownloadRepositoryAdapter implements ApkDownloadRepository {

    private final ApkDownloadJpaRepository jpaRepository;

    @Override
    public ApkDownload save(ApkDownload download) {
        ApkDownloadJpaEntity entity = new ApkDownloadJpaEntity(
            download.getId(),
            download.getPlatform(),
            download.getIpAddress(),
            download.getUserAgent(),
            download.getDownloadedAt()
        );

        ApkDownloadJpaEntity saved = jpaRepository.save(entity);

        return new ApkDownload(
            saved.getId(),
            saved.getPlatform(),
            saved.getIpAddress(),
            saved.getUserAgent(),
            saved.getDownloadedAt()
        );
    }

    @Override
    public long countByPlatform(String platform) {
        return jpaRepository.countByPlatform(platform);
    }

    @Override
    public boolean existsRecentDownloadByIp(String ipAddress, String platform, Instant since) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return false;
        }
        return jpaRepository.existsByIpAddressAndPlatformAndDownloadedAtAfter(ipAddress, platform, since);
    }
}
