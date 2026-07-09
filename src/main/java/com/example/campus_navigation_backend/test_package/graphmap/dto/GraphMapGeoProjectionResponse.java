package com.example.campus_navigation_backend.test_package.graphmap.dto;

/**
 * 세계 측지 좌표계로 표현된 투영 미리보기 응답이다.
 */
public record GraphMapGeoProjectionResponse(
        GraphMapGeoPointRequest inputPoint,
        GraphMapGeoPointResponse projectedPoint,
        long edgeFromNodeId,
        long edgeToNodeId,
        double distanceToEdge,
        double accessCostToFromNode,
        double accessCostToToNode
) {
}
