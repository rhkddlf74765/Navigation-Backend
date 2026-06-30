package com.example.campus_navigation_backend.repository.dto;

import com.example.campus_navigation_backend.domain.graph.Point3D;

import java.util.List;

/**
 * 분할된 최종 엣지 테이블에서 조회한 행이다.
 */
public record GraphEdgeRow(
        long id,
        long originalEdgeId,
        String highway,
        long source,
        long target,
        double cost,
        List<Point3D> geometry
) {
}
