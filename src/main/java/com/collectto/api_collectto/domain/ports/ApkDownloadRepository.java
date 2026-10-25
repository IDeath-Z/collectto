package com.collectto.api_collectto.domain.ports;

import com.collectto.api_collectto.domain.entities.ApkDownload;

public interface ApkDownloadRepository {

    ApkDownload save(ApkDownload download);

    long countByPlatform(String platform);
}
