package com.example.campus_navigation_backend.application.routing.helper;

/**
 * API 입력값을 metric 좌표 기반으로 해석한 출발 endpoint와 도착 endpoint를 함께 보관한다.
 *
 * @param start 해석된 출발 endpoint 후보
 * @param destination 해석된 도착 endpoint 후보
 */
public record ResolvedRouteRequest(
        ResolvedRouteEndpoint start,
        ResolvedRouteEndpoint destination
) {
}
