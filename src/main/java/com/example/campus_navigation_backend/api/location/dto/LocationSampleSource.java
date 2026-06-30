package com.example.campus_navigation_backend.api.location.dto;

/**
 * 위치 샘플이 어떤 상황에서 생성됐는지 구분하는 타입이다.
 */
public enum LocationSampleSource {
    /**
     * 사용자가 반환된 경로를 따라 이동하는 동안 주기적으로 수집한 위치이다.
     */
    ROUTE_TRACKING,

    /**
     * 사용자가 지도에서 특정 지점을 클릭했을 때 수집한 위치이다.
     */
    MAP_CLICK,

    /**
     * GPS 오차 측정이나 보정 실험을 위해 수집한 위치이다.
     */
    CALIBRATION
}
