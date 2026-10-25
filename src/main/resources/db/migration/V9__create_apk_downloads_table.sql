CREATE TABLE apk_downloads (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    platform VARCHAR(50) NOT NULL DEFAULT 'android',
    ip_address VARCHAR(100),
    user_agent VARCHAR(500),
    downloaded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_apk_downloads_platform ON apk_downloads(platform);
