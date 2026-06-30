package com.example.campus_navigation_backend.api.graphmap.dto;

/**
 * 계산된 경로에 포함된 하나의 엣지 통행 단계를 표현한다.
 */
public record GraphMapRouteStepResponse(
        long fromNodeId,
        long toNodeId,
        double cost
) {
}
