package com.example.campus_navigation_backend.domain.graph;

/**
 * metric 좌표계에서 사용하는 3차원 좌표 값 객체이다.
 *
 * @param x x 좌표
 * @param y y 좌표
 * @param z z 좌표
 */
public record Point3D(
        double x, double y, double z
) {
    /**
     * 다른 좌표와의 2D 평면 거리를 계산한다.
     *
     * @param other 비교 대상 좌표
     * @return 2D 거리
     */
    public double distance2D(Point3D other) {
        return Math.hypot(x - other.x, y - other.y);
    }

    /**
     * 다른 좌표와의 3D 공간 거리를 계산한다.
     *
     * @param other 비교 대상 좌표
     * @return 3D 거리
     */
    public double distance3D(Point3D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
