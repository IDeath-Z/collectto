package com.collectto.api_collectto.infrastructure.persistence.downloads;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApkDownloadJpaRepository extends JpaRepository<ApkDownloadJpaEntity, Long> {

    long countByPlatform(String platform);
}
