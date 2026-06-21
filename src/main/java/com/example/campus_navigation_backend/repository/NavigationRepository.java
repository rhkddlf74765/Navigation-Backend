package com.example.campus_navigation_backend.repository;

import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.repository.dto.CurrentProjectionRow;
import com.example.campus_navigation_backend.repository.dto.EntranceProjectionRow;
import com.example.campus_navigation_backend.repository.dto.LineSegmentRow;
import com.example.campus_navigation_backend.repository.dto.TransformedPointRow;
import com.example.campus_navigation_backend.visualizer.Wgs84PointRow;

import java.util.List;

/**
 * PostGIS 기반 navigation 데이터를 application/domain layer에서 사용할 DTO로 조회하는 저장소 계약이다.
 */
public interface NavigationRepository {

    /**
     * 그래프 초기화에 사용할 보행 가능 도로 선분을 조회한다.
     *
     * @return 보행 가능 도로 선분 목록
     */
    List<LineSegmentRow> findAllWalkableLineSegments();

    /**
     * 건물 출입구와 가장 가까운 보행로 투영 정보를 조회한다.
     *
     * @return 출입구 투영 정보 목록
     */
    List<EntranceProjectionRow> findAllEntranceProjections();

    /**
     * WGS84 좌표를 경로 계산용 metric 좌표계로 변환한다.
     *
     * @param longitude 경도
     * @param latitude 위도
     * @param altitude 고도
     * @return metric 좌표 변환 결과
     */
    TransformedPointRow transformToMetric(double longitude, double latitude, double altitude);

    /**
     * 현재 위치 metric 좌표를 가장 가까운 보행로 위에 투영한다.
     *
     * @param x metric x 좌표
     * @param y metric y 좌표
     * @param z metric z 좌표
     * @return 현재 위치 투영 결과
     */
    CurrentProjectionRow projectCurrentLocationToNearestLine(double x, double y, double z);

    /**
     * metric 좌표 경로를 지도 시각화용 WGS84 좌표로 변환한다.
     *
     * @param metricPoints metric 좌표 목록
     * @return WGS84 좌표 목록
     */
    List<Wgs84PointRow> transformMetricPointsToWgs84(List<Point3D> metricPoints);
}
