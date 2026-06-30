package com.example.campus_navigation_backend.application.routing.helper;

/**
 * 접근 경로와 A* 그래프 경로를 결합한 하나의 완성된 라우팅 후보를 보관한다.
 *
 * @param destinationEndpointNodeId 도착 측에서 선택된 그래프 endpoint 노드 ID
 * @param startAccessCost 원본 출발 endpoint에서 선택된 출발 그래프 endpoint 노드까지의 비용
 * @param graphCost 선택된 그래프 endpoint 노드 사이의 A* 경로 비용
 * @param destinationAccessCost 선택된 도착 그래프 endpoint 노드에서 원본 도착 endpoint까지의 비용
 * @param routePath 전체 경로와 비용을 담은 경로 객체
 */
public record RouteCandidateResult(
        long destinationEndpointNodeId,
        double startAccessCost,
        double graphCost,
        double destinationAccessCost,
        RoutePath routePath
) {
    /**
     * 접근 경로 비용과 그래프 경로 비용이 이미 결합된 전체 경로 비용을 반환한다.
     */
    public double totalCost() {
        return routePath.cost();
    }
}
