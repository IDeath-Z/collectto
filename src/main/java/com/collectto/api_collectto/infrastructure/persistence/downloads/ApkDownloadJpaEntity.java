package com.collectto.api_collectto.infrastructure.persistence.downloads;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "apk_downloads")
public class ApkDownloadJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String platform;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "downloaded_at", nullable = false, insertable = false, updatable = false)
    @CreationTimestamp
    private Instant downloadedAt;

    public ApkDownloadJpaEntity(Long id, String platform, String ipAddress, String userAgent, Instant downloadedAt) {
        this.id = id;
        this.platform = platform;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.downloadedAt = downloadedAt;
    }
}
