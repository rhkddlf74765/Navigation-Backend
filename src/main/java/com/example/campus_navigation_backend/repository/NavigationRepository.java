package com.example.campus_navigation_backend.repository;

import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.repository.dto.CurrentProjectionRow;
import com.example.campus_navigation_backend.repository.dto.EntranceProjectionRow;
import com.example.campus_navigation_backend.repository.dto.LineSegmentRow;
import com.example.campus_navigation_backend.repository.dto.TransformedPointRow;
import com.example.campus_navigation_backend.visualizer.Wgs84PointRow;

import java.util.List;

/**
 * DB 데이터를 DTO로 반환
 */
public interface NavigationRepository {
    /**
     * all_lines_split 조회
     * entrance와 nearest line projection 조회
     * 현재 위치 좌표 변환
     * 현재 위치를 nearest line에 projection
     * 필요 시 building / entrance 관련 조회
     */

    List<LineSegmentRow> findAllWalkableLineSegments();

    List<EntranceProjectionRow> findAllEntranceProjections();

    TransformedPointRow transformToMetric(double longitude, double latitude, double altitude);

    CurrentProjectionRow projectCurrentLocationToNearestLine(double x, double y, double z);

    /**
     * 검증용 웹 기능을 위해 추가한다.
     * 실제 서비스의 비즈니스 로직에 포함되지는 않는다.
     * @param metricPoints
     * @return
     */
//    List<Wgs84PointRow> transformMetricPointsToWgs84(List<Point3D> metricPoints);

}
