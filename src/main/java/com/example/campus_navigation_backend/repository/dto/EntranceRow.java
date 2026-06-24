package com.example.campus_navigation_backend.repository.dto;

import com.example.campus_navigation_backend.domain.graph.Point3D;

/**
 * DB의 건물 입구 행을 metric 좌표로 표현한 DTO이다.
 *
 * @param entranceId 입구 ID
 * @param description 건물명 또는 입구 설명
 * @param nodeType 입구 노드 유형
 * @param x metric x 좌표
 * @param y metric y 좌표
 * @param z metric z 좌표
 */
public record EntranceRow(
        long entranceId,
        String description,
        String nodeType,
        double x,
        double y,
        double z
) {
    /**
     * 입구 좌표를 Point3D 값 객체로 변환한다.
     *
     * @return 입구 좌표
     */
    public Point3D point() {
        return new Point3D(x, y, z);
    }
}
