package com.example.campus_navigation_backend.application.routing.helper;

import com.example.campus_navigation_backend.application.dto.RouteEndpointType;
import com.example.campus_navigation_backend.domain.graph.Point3D;

import java.util.List;

/**
 * 요청 endpoint를 metric 그래프 좌표 지점으로 해석한 결과를 보관한다.
 *
 * @param type 해당 endpoint가 어떤 방식으로 해석되었는지 추적하기 위한 원본 endpoint 타입
 * @param points 인접 그래프 엣지에 투영할 수 있는 metric 후보 지점 목록
 * @param displayName 응답에 표시할 endpoint 이름이며, 건물 endpoint일 때 건물명을 보관한다
 */
public record ResolvedRouteEndpoint(
        RouteEndpointType type,
        List<Point3D> points,
        String displayName
) {
}
