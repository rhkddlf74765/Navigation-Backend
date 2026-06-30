package com.example.campus_navigation_backend.application.routing.helper;

import java.util.List;

/**
 * 해석된 라우팅 endpoint에 대해 출발 측과 도착 측의 모든 접근 경로를 함께 보관한다.
 *
 * @param startAccessPaths 출발 좌표 또는 건물 지점에서 그래프 endpoint 노드까지의 접근 경로
 * @param destinationAccessPaths 그래프 endpoint 노드에서 도착 좌표 또는 건물 지점까지의 접근 경로
 */
public record EndpointAccessPaths(
        List<EndpointAccessPath> startAccessPaths,
        List<EndpointAccessPath> destinationAccessPaths
) {
}
