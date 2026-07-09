package com.example.campus_navigation_backend.domain.graph;

/**
 * 미터 단위 좌표계에서 사용하는 3차원 좌표 값 객체이다.
 *
 * @param x x 좌표
 * @param y y 좌표
 * @param z z 좌표
 */
public record Point3D(
        double lon, double lat, double ele
) {
    /**
     * 다른 좌표와의 2D 평면 거리를 계산한다.
     *
     * @param other 비교 대상 좌표
     * @return 2D 거리
     */
    public double distance2D(Point3D other) {
        return Math.hypot(lon - other.lon, lat - other.lat);
    }

    /**
     * 다른 좌표와의 3D 공간 거리를 계산한다.
     *
     * @param other 비교 대상 좌표
     * @return 3D 거리
     */
    public double distance3D(Point3D other) {
        double dx = lon - other.lon;
        double dy = lat - other.lat;
        double dz = ele - other.ele;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
