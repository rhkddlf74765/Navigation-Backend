package com.example.campus_navigation_backend.repository.dto;

import com.example.campus_navigation_backend.domain.graph.Point3D;

/**
 * 이름이 있는 건물 라우팅 지점으로 사용할 수 있는 데이터베이스 row를 표현한다.
 *
 * @param id 원본 노드 또는 지점 식별자
 * @param buildingName 라우팅 요청에서 이 지점을 찾기 위해 사용하는 건물명
 * @param x metric x 좌표
 * @param y metric y 좌표
 * @param z metric z 좌표
 */
public record BuildingPointRow(
        long id,
        String buildingName,
        double x,
        double y,
        double z
) {
    /**
     * 응용 라우팅이 행 DTO에 의존하지 않도록 이 행을 그래프 metric 지점 타입으로 변환한다.
     */
    public Point3D point() {
        return new Point3D(x, y, z);
    }
}
