package com.example.campus_navigation_backend.application;

/**
 * 임의 좌표와 그래프 endpoint 노드 사이의 접근 경로를 표현한다.
 * <p>
 * 출발 접근 경로는 원본 출발 좌표에서 endpoint까지의 방향이고, 도착 접근 경로는
 * endpoint에서 원본 도착 좌표까지의 방향이다.
 *
 * @param endpointNodeId A* 탐색의 시작 또는 도착으로 사용할 그래프 노드 ID
 * @param routePath endpoint까지의 접근 비용과 좌표 목록
 */
public record EndpointAccessPath(
        long endpointNodeId,
        RoutePath routePath
) {
}
