package com.example.campus_navigation_backend.api.location.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * 클라이언트가 서버로 전송하는 GPS 추정 좌표와 실제 좌표 로그 요청이다.
 *
 * @param userId 위치 샘플을 전송한 사용자 ID
 * @param routeSessionId 위치 샘플이 연결되는 라우팅 세션 ID
 * @param gps 클라이언트가 추정한 GPS 좌표
 * @param actual 비교 기준으로 사용하는 실제 좌표
 * @param recordedAt 클라이언트가 위치를 측정한 시각
 */
public record LocationSampleRequest(

        @NotNull
        UUID userId,

        UUID routeSessionId,

        @Valid
        @NotNull
        LocationCoordinateRequest gps,

        @Valid
        LocationCoordinateRequest actual,

        Instant recordedAt
) {
}
