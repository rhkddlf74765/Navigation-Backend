package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.domain.graph.Point3D;

import java.util.List;

/**
 * 최종 경로를 구성하는 하나의 부분 경로와 그 이동 비용이다.
 *
 * @param cost 부분 경로 이동 비용
 * @param path 부분 경로를 구성하는 metric 좌표 목록
 */
public record RouteSegment(
        double cost,
        List<Point3D> path
) {
}
