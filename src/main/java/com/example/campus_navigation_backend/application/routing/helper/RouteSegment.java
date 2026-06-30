package com.example.campus_navigation_backend.application.routing.helper;

import com.example.campus_navigation_backend.domain.graph.Point3D;

import java.util.List;

/**
 * 라우팅 전처리 중 생성되는 하나의 부분 경로 구간을 표현한다.
 *
 * @param cost 해당 구간의 가중 비용
 * @param path 해당 구간을 구성하는 metric 좌표 목록
 */
public record RouteSegment(
        double cost,
        List<Point3D> path
) {
}
