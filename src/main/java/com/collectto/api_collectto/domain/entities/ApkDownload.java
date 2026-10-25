package com.collectto.api_collectto.domain.entities;

import java.time.Instant;

import com.collectto.api_collectto.domain.shared.DomainValidator;

public final class ApkDownload {

    private final Long id;
    private final String platform;
    private final String ipAddress;
    private final String userAgent;
    private final Instant downloadedAt;

    public ApkDownload(Long id, String platform, String ipAddress, String userAgent, Instant downloadedAt) {
        this.id = id;
        this.platform = DomainValidator.requireNonNull(platform, "Platform cannot be null");
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.downloadedAt = DomainValidator.requireNonNull(downloadedAt, "DownloadedAt cannot be null");
    }

    public static ApkDownload create(String platform, String ipAddress, String userAgent) {
        String resolvedPlatform = (platform == null || platform.isBlank()) ? "android" : platform.trim().toLowerCase();
        return new ApkDownload(null, resolvedPlatform, ipAddress, userAgent, Instant.now());
    }

    public Long getId() {
        return id;
    }

    public String getPlatform() {
        return platform;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Instant getDownloadedAt() {
        return downloadedAt;
    }
}
