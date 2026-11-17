package com.freeconnect.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracion del almacenamiento de portafolios (S3-compatible o local).
 */
@ConfigurationProperties(prefix = "freeconnect.storage")
public record StorageProperties(String bucket, String endpoint, String publicBaseUrl, String provider,
                                String localPath) {

    public StorageProperties {
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            publicBaseUrl = "http://localhost:8080/assets";
        }
        if (provider == null || provider.isBlank()) {
            provider = "filesystem";
        }
        if (localPath == null || localPath.isBlank()) {
            localPath = "./data/portfolio";
        }
    }
}
