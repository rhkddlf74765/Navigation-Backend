package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.projection.PointProjection;

/**
 * 한 그래프 엣지와 해당 엣지 위로 좌표를 projection한 결과를 함께 보관한다.
 *
 * @param edge projection 대상 엣지
 * @param projection 좌표를 edge 위에 projection한 결과
 */
public record ProjectedEdgeCandidate(
        GraphEdge edge,
        PointProjection projection
) {
}
