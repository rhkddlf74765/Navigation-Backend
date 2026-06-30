package com.example.campus_navigation_backend.api.location.dto;

import java.time.Instant;

/**
 * 위치 샘플 저장 결과를 클라이언트에 반환하는 응답이다.
 *
 * @param id 저장된 위치 샘플 로그 ID
 * @param errorMeters GPS 추정 좌표와 실제 좌표 사이의 거리 오차
 * @param receivedAt 서버가 위치 샘플을 수신한 시각
 */
public record LocationSampleResponse(
        Long id,
        Double errorMeters,
        Instant receivedAt
) {
}
