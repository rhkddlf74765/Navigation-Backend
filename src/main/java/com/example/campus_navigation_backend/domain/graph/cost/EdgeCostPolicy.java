package com.example.campus_navigation_backend.domain.graph.cost;

/**
 * DB의 실제 2D 거리(m)를 기준으로 방향별 라우팅 비용을 계산하는 전략 인터페이스이다.
 *
 * 반환되는 cost는 A*가 사용하는 가중치이며 반드시 distanceMeters 이상이어야 한다.
 * 평지 등 추가 패널티가 없는 경우에는 distanceMeters를 그대로 반환할 수 있다.
 */
public interface EdgeCostPolicy {

    /**
     * 정책별 보정 규칙을 적용해 방향별 엣지 비용을 계산한다.
     *
     * @param highway OSM highway 값
     * @param distanceMeters DB에 저장된 실제 2D 거리(m)
     * @param elevationDelta 이동 방향 기준 고도 차이(m)
     * @return A*가 가중치로 사용할 통행 비용. 항상 distanceMeters 이상이어야 한다.
     */
    double calculate(
            String highway,
            double distanceMeters,
            double elevationDelta
    );
}