package com.example.campus_navigation_backend.application.routing.helper;

/**
 * 원본 endpoint 지점과 그래프 endpoint 노드 사이의 접근 경로를 표현한다.
 * <p>
 * 출발 측에서는 원본 출발 지점에서 그래프 endpoint 노드로 향하는 경로이고,
 * 도착 측에서는 그래프 endpoint 노드에서 원본 도착 지점으로 향하는 경로이다.
 *
 * @param endpointNodeId A* 탐색의 출발 또는 도착으로 사용할 그래프 노드 ID
 * @param routePath 원본 endpoint 지점과 그래프 노드 사이를 연결하는 접근 경로와 비용
 */
public record EndpointAccessPath(
        long endpointNodeId,
        RoutePath routePath
) {
}
