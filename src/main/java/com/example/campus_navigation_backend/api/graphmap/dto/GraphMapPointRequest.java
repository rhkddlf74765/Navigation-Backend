package com.example.campus_navigation_backend.api.graphmap.dto;

import com.example.campus_navigation_backend.domain.graph.Point3D;

/**
 * 그래프 시각화 좌표계에서 전달되는 지도 클릭 지점이다.
 */
public record GraphMapPointRequest(double x, double y, Double z) {

    public Point3D toPoint3D() {
        return new Point3D(x, y, z == null ? 0.0 : z);
    }
}
