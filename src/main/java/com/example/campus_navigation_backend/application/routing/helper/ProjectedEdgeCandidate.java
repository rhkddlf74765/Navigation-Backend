package com.example.campus_navigation_backend.application.routing.helper;

import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.projection.PointProjection;

/**
 * 임의의 endpoint 지점과 인접한 그래프 엣지 후보 및 그 엣지 위 투영 결과를 함께 보관한다.
 *
 * @param edge endpoint 지점과 인접한 그래프 엣지 후보
 * @param projection endpoint 지점을 엣지 위에 투영한 결과
 */
public record ProjectedEdgeCandidate(
        GraphEdge edge,
        PointProjection projection
) {
}
