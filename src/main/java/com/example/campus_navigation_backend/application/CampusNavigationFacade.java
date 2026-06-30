package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.application.dto.RoutePoint;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.config.NavigationProperties;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.repository.NavigationRepository;
import com.example.campus_navigation_backend.repository.dto.TransformedPointRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampusNavigationFacade {

    private final NavigationRepository navigationRepository;
    private final NavigationProperties properties;
    private final NearbyEdgeFinder nearbyEdgeFinder;
    private final EndpointAccessPathFactory endpointAccessPathFactory;
    private final RouteCandidateEvaluator routeCandidateEvaluator;

    /**
     * 출발 좌표와 도착 좌표를 metric 좌표로 변환한 뒤, 각 좌표의 인접 엣지 후보를 기반으로
     * 접근 경로와 그래프 내부 A* 경로를 조합해 최단 경로를 찾는다.
     *
     * @param request 출발 좌표와 도착 좌표를 포함한 경로 탐색 요청
     * @return 접근 경로, 그래프 내부 경로, 전체 geometry를 포함한 경로 응답
     */
    public RouteResponse findRoute(RouteRequest request) {
        Point3D startPoint = transformToMetric(
                request.longitude(),
                request.latitude(),
                request.altitude()
        );
        Point3D destinationPoint = transformToMetric(
                request.destinationLongitude(),
                request.destinationLatitude(),
                request.destinationAltitude()
        );

        int nearbyEdgeLimit = Math.max(1, properties.maxCandidates());
        List<ProjectedEdgeCandidate> startEdges = nearbyEdgeFinder.findNearbyEdges(startPoint, nearbyEdgeLimit);
        List<ProjectedEdgeCandidate> destinationEdges = nearbyEdgeFinder.findNearbyEdges(destinationPoint, nearbyEdgeLimit);

        List<EndpointAccessPath> startAccessPaths =
                endpointAccessPathFactory.createStartAccessPaths(startPoint, startEdges);
        List<EndpointAccessPath> destinationAccessPaths =
                endpointAccessPathFactory.createDestinationAccessPaths(destinationPoint, destinationEdges);

        RouteCandidateResult bestRoute =
                routeCandidateEvaluator.findBestRoute(startAccessPaths, destinationAccessPaths);

        return toResponse(request, bestRoute);
    }

    /**
     * 클라이언트가 전달한 WGS84 좌표를 그래프 탐색에 사용하는 metric 좌표계로 변환한다.
     *
     * @param longitude WGS84 경도
     * @param latitude WGS84 위도
     * @param altitude 고도. null이면 0으로 처리한다.
     * @return metric 좌표계의 3차원 점
     */
    private Point3D transformToMetric(Double longitude, Double latitude, Double altitude) {
        if (longitude == null || latitude == null) {
            throw new IllegalArgumentException("Route coordinates must not be null.");
        }

        TransformedPointRow transformed = navigationRepository.transformToMetric(
                longitude,
                latitude,
                altitude == null ? 0.0 : altitude
        );

        return transformed.toPoint3D();
    }

    /**
     * 내부 라우팅 후보 결과를 외부 API 응답 DTO로 변환한다.
     *
     * @param request 원본 경로 요청
     * @param bestRoute 최종 선택된 후보 경로
     * @return 경로 탐색 응답
     */
    private RouteResponse toResponse(RouteRequest request, RouteCandidateResult bestRoute) {
        return new RouteResponse(
                request.destinationBuildingName(),
                bestRoute.destinationEndpointNodeId(),
                bestRoute.totalCost(),
                bestRoute.startAccessCost() + bestRoute.destinationAccessCost(),
                bestRoute.graphCost(),
                toRoutePoints(bestRoute.routePath().points())
        );
    }

    /**
     * metric 좌표 path를 응답용 좌표 DTO 목록으로 변환한다.
     *
     * @param points metric 좌표 목록
     * @return 응답에 포함할 경로 좌표 목록
     */
    private List<RoutePoint> toRoutePoints(List<Point3D> points) {
        return points.stream()
                .map(point -> new RoutePoint(point.x(), point.y(), point.z()))
                .toList();
    }
}
