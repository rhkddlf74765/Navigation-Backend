package com.example.campus_navigation_backend.api.graphmap.dto;

/**
 * 클릭한 지점과 가장 가까운 엣지에 대한 투영 결과이다.
 */
public record GraphMapProjectionResponse(
        GraphMapPointRequest inputPoint,
        GraphMapPointResponse projectedPoint,
        long edgeFromNodeId,
        long edgeToNodeId,
        double distanceToEdge,
        double accessCostToFromNode,
        double accessCostToToNode
) {
}
