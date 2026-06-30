package com.example.campus_navigation_backend.api.location.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * 프론트에서 서버로 전송하는 위치 샘플 저장 요청이다.
 * <p>
 * GPS 추정 좌표는 항상 필요하고, 실제 기준 좌표는 지도 클릭이나 보정 실험처럼 기준 좌표를 알 수 있는 경우에만 전달한다.
 *
 * @param routeSessionId 위치 샘플이 연결되는 라우팅 세션 ID
 * @param gps 프론트에서 측정한 GPS 추정 좌표
 * @param actual 비교 기준으로 사용할 실제 좌표
 * @param source 위치 샘플이 발생한 상황
 * @param recordedAt 프론트가 위치를 측정한 시각
 */
public record LocationSampleRequest(
        UUID routeSessionId,

        @Valid
        @NotNull
        LocationCoordinateRequest gps,

        @Valid
        LocationCoordinateRequest actual,

        LocationSampleSource source,
        Instant recordedAt
) {
}
