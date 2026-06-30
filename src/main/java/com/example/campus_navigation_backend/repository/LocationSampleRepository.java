package com.example.campus_navigation_backend.repository;

import com.example.campus_navigation_backend.api.location.dto.LocationSampleRequest;

import java.time.Instant;

/**
 * 위치 샘플 로그를 영속화하는 저장소 인터페이스이다.
 */
public interface LocationSampleRepository {

    /**
     * 위치 샘플과 계산된 GPS 오차를 저장하고 생성된 로그 ID를 반환한다.
     */
    Long save(LocationSampleRequest request, Double errorMeters, Instant receivedAt);
}
