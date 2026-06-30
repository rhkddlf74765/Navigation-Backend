package com.example.campus_navigation_backend.application.routing.helper;

import com.example.campus_navigation_backend.application.building.BuildingPointStore;
import com.example.campus_navigation_backend.application.dto.RouteEndpointRequest;
import com.example.campus_navigation_backend.application.dto.RouteEndpointType;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.repository.NavigationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 원본 라우팅 요청을 라우팅 파이프라인에서 사용할 metric 좌표와 endpoint 후보로 해석한다.
 * <p>
 * 좌표 endpoint는 WGS84에서 그래프 좌표계로 변환하고, 건물 endpoint는 메모리에 등록된 건물 지점 목록으로 확장한다.
 */
@Component
@RequiredArgsConstructor
public class RouteRequestResolver {

    private final NavigationRepository navigationRepository;
    private final BuildingPointStore buildingPointStore;

    /**
     * 인접 엣지 투영을 시작하기 전에 라우팅 요청의 endpoint가 모두 존재하는지 검증하고 각각을 해석한다.
     */
    public ResolvedRouteRequest resolve(RouteRequest request) {
        if (request.start() == null) {
            throw new IllegalArgumentException("Start endpoint is required.");
        }
        if (request.destination() == null) {
            throw new IllegalArgumentException("Destination endpoint is required.");
        }

        return new ResolvedRouteRequest(
                resolveEndpoint(request.start()),
                resolveEndpoint(request.destination())
        );
    }

    /**
     * 좌표 endpoint와 건물 endpoint의 검증 규칙이 섞이지 않도록 타입에 따라 해석 로직을 분기한다.
     */
    private ResolvedRouteEndpoint resolveEndpoint(RouteEndpointRequest endpoint) {
        if (endpoint.type() == null) {
            throw new IllegalArgumentException("Endpoint type is required.");
        }

        return switch (endpoint.type()) {
            case COORDINATE -> resolveCoordinate(endpoint);
            case BUILDING -> resolveBuilding(endpoint);
        };
    }

    /**
     * 그래프 투영과 A*는 metric 그래프 좌표에서 동작하므로 좌표 endpoint를 metric 좌표계로 변환한다.
     */
    private ResolvedRouteEndpoint resolveCoordinate(RouteEndpointRequest endpoint) {
        if (endpoint.longitude() == null || endpoint.latitude() == null) {
            throw new IllegalArgumentException("Longitude and latitude are required for coordinate endpoint.");
        }

        Point3D metricPoint = navigationRepository.transformToMetric(
                endpoint.longitude(),
                endpoint.latitude(),
                endpoint.altitude() == null ? 0.0 : endpoint.altitude()
        ).toPoint3D();

        return new ResolvedRouteEndpoint(RouteEndpointType.COORDINATE, List.of(metricPoint), null);
    }

    /**
     * 평가기가 여러 출입구 후보를 비교해 최적 출입구 또는 대표 지점을 선택할 수 있도록 건물 endpoint를 모든 건물 지점으로 확장한다.
     */
    private ResolvedRouteEndpoint resolveBuilding(RouteEndpointRequest endpoint) {
        if (endpoint.buildingName() == null || endpoint.buildingName().isBlank()) {
            throw new IllegalArgumentException("Building name is required for building endpoint.");
        }

        List<Point3D> points = buildingPointStore.findByBuildingName(endpoint.buildingName());
        if (points.isEmpty()) {
            throw new IllegalArgumentException("No building point found: " + endpoint.buildingName());
        }

        return new ResolvedRouteEndpoint(RouteEndpointType.BUILDING, points, endpoint.buildingName());
    }
}
