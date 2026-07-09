package com.example.campus_navigation_backend.service;

import com.example.campus_navigation_backend.application.dto.RoutePoint;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.repository.NavigationRepository;
import com.example.campus_navigation_backend.visualizer.MapPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 경로 좌표를 지도 표시용 좌표계로 변환하는 서비스이다.
 */
@Service
@RequiredArgsConstructor
public class CoordinateTransformService {
    private final NavigationRepository navigationRepository;

    /**
     * 경로 응답의 미터 단위 좌표 목록을 WGS84 위경도 좌표 목록으로 변환한다.
     *
     * @param routePoints metric 좌표 경로
     * @return WGS84 좌표 경로
     */
    public List<MapPoint> metricRouteToWgs84(List<RoutePoint> routePoints) {
        List<Point3D> metricPoints = routePoints.stream()
                .map(point -> new Point3D(point.lon(), point.lat(), point.ele()))
                .toList();

        return navigationRepository.transformMetricPointsToWgs84(metricPoints).stream()
                .map(row -> row.toMapPoint())
                .toList();
    }
}
