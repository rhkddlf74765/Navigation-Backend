package com.example.campus_navigation_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "navigation"
)
public record NavigationProperties(
        int requestSrid,
        int metricSrid,
        double projectionRadiusMeters,
        int projectionMaxCandidates,
        double spatialIndexCellSizeMeters
) {
}