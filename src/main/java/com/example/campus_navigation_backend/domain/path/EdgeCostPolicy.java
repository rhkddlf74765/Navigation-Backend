package com.example.campus_navigation_backend.domain.path;

/**
 * 엣지 통행 비용을 계산하는 전략 인터페이스이다.
 */
public interface EdgeCostPolicy {

    /**
     * 정책별 보정 규칙을 적용해 엣지 비용을 계산한다.
     *
     * @param edgeType 엣지 유형
     * @param highway OSM highway 값
     * @param baseCost 기본 geometry 비용
     * @param elevationDelta 이동 방향 기준 고도 차이
     * @return 보정된 통행 비용
     */
    double calculate(String edgeType, String highway, double baseCost, double elevationDelta);
}
