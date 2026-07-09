package com.example.campus_navigation_backend.api.location.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 위치 샘플 요청에서 사용하는 단일 좌표 값을 표현한다.
 * <p>
 * GPS 좌표에서는 정확도 값을 함께 사용할 수 있고, 실제 기준 좌표에서는 경도와 위도 중심으로 오차 계산에 사용한다.
 *
 * @param longitude 경도
 * @param latitude 위도
 * @param altitude 고도
 * @param accuracyMeters GPS 측정 정확도
 */
public record LocationCoordinateRequest(
        @NotNull Double lon,
        @NotNull Double lat,
        Double ele
) {
}
