package com.example.campus_navigation_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 시스템에서 전역적으로 사용할 수 있는 설정 파일
 * @param requestSrid
 * @param metricSrid
 * @param candidateRadiusMeters
 * @param maxCandidates
 * @param walkableHighways
 */
@ConfigurationProperties(prefix = "navigation")
public record NavigationProperties(
    int requestSrid,
    int metricSrid,
    double candidateRadiusMeters,
    int maxCandidates,
    List<String> walkableHighways
) { }
