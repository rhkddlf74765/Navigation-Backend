package com.example.campus_navigation_backend.application;

/**
 * 출발 접근 경로, 그래프 내부 A* 경로, 도착 접근 경로를 모두 합친 하나의 라우팅 후보 결과이다.
 *
 * @param destinationEndpointNodeId 선택된 도착 측 그래프 endpoint 노드 ID
 * @param startAccessCost 출발 좌표에서 출발 endpoint까지의 접근 비용
 * @param graphCost 출발 endpoint에서 도착 endpoint까지의 그래프 내부 경로 비용
 * @param destinationAccessCost 도착 endpoint에서 도착 좌표까지의 접근 비용
 * @param routePath 전체 경로의 비용과 좌표 목록
 */
public record RouteCandidateResult(
        long destinationEndpointNodeId,
        double startAccessCost,
        double graphCost,
        double destinationAccessCost,
        RoutePath routePath
) {
    public double totalCost() {
        return routePath.cost();
    }
}
