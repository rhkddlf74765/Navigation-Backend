package com.example.campus_navigation_backend.log.domain;

import jakarta.persistence.Embeddable;

/**
 * 로그에서 사용하는 경도, 위도, 고도 좌표 값이다.
 */
@Embeddable
public record PointLog(
        Double lon,
        Double lat,
        Double ele
) {
    /**
     * 좌표 로그 값을 생성한다.
     */
    public static PointLog of(Double lon, Double lat, Double ele) {
        return new PointLog(lon, lat, ele);
    }
}
