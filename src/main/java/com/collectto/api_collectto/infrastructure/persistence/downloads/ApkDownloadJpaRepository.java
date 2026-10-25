package com.collectto.api_collectto.infrastructure.persistence.downloads;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApkDownloadJpaRepository extends JpaRepository<ApkDownloadJpaEntity, Long> {

    long countByPlatform(String platform);

    boolean existsByIpAddressAndPlatformAndDownloadedAtAfter(String ipAddress, String platform, Instant downloadedAt);
}
