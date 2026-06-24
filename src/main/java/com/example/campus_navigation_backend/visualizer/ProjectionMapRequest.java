package com.example.campus_navigation_backend.visualizer;

import jakarta.validation.constraints.NotNull;

/**
 * 지도에서 클릭한 WGS84 좌표를 그래프 엣지에 투영하기 위한 요청이다.
 *
 * @param latitude 위도
 * @param longitude 경도
 * @param altitude 고도
 */
public record ProjectionMapRequest(
        @NotNull Double latitude,
        @NotNull Double longitude,
        Double altitude
) {
    /**
     * 고도가 생략된 경우 0으로 보정한다.
     *
     * @return 보정된 고도
     */
    public double resolvedAltitude() {
        return altitude == null ? 0.0 : altitude;
    }
}
