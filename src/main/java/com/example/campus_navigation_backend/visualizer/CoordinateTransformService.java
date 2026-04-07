package com.example.campus_navigation_backend.visualizer;

import com.example.campus_navigation_backend.application.dto.RoutePoint;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.repository.NavigationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoordinateTransformService {
    private final NavigationRepository navigationRepository;

    public List<MapPoint> metricRouteToWgs84(List<RoutePoint> routePoints) {
        List<Point3D> metricPoints = routePoints.stream()
                .map(point -> new Point3D(point.x(), point.y(), point.z()))
                .toList();

        return navigationRepository.transformMetricPointsToWgs84(metricPoints).stream()
                .map(row -> row.toMapPoint())
                .toList();
    }
}
