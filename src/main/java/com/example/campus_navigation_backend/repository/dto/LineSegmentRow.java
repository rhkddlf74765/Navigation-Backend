package com.example.campus_navigation_backend.repository.dto;

import com.example.campus_navigation_backend.domain.graph.Point3D;

import java.util.List;

public record LineSegmentRow(
        long lineId,
        String highway,
        double startX,
        double startY,
        double startZ,
        double endX,
        double endY,
        double endZ,
        double cost,
        List<Point3D> geometry
) {
    public Point3D startPoint() {
        return new Point3D(startX, startY, startZ);
    }

    public Point3D endPoint() {
        return new Point3D(endX, endY, endZ);
    }

    /**
     * endPoint와 startPoint의 고도 차를 반환한다.
     * <p> elevation > 0 : start -> end 방향은 오르막
     * <p> elevation < 0 : start -> end 방향은 내리막
     * <p> elevation == 0 : 평지
     * @return
     */
    public double elevationDelta() {
        return endZ - startZ;
    }
}
