package com.example.campus_navigation_backend.application.building;

import com.example.campus_navigation_backend.domain.graph.Point3D;

import java.util.List;

/**
 * 하나의 표시용 건물명과 그 건물에 연결된 지점 목록을 함께 보관한다.
 *
 * @param displayName 클라이언트에 보여줄 원본 건물명
 * @param points 해당 건물의 출입구 또는 대표 지점 목록
 */
public record BuildingPointGroup(
        String displayName,
        List<Point3D> points
) {
}
