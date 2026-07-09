package com.example.campus_navigation_backend.visualizer;

import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.application.routing.CampusNavigationFacade;
import com.example.campus_navigation_backend.service.CoordinateTransformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 기본 라우팅 결과를 지도 시각화에서 사용하는 WGS84 경로 응답 형태로 변환한다.
 */
@Service
@RequiredArgsConstructor
public class RouteMapFacade {

    private final CampusNavigationFacade campusNavigationFacade;
    private final CoordinateTransformService coordinateTransformService;

    /**
     * 시각화 화면은 지리 좌표 기반 지도에 경로를 렌더링하므로,
     * 응용 facade를 통해 경로를 찾은 뒤 metric 경로를 WGS84로 변환한다.
     */
    public RouteMapResponse findRouteForMap(RouteRequest request) {
        RouteResponse routeResponse = campusNavigationFacade.findRoute(request);

        List<MapPoint> mapPath = coordinateTransformService.metricRouteToWgs84(routeResponse.path());

        MapPoint startPoint = resolveStartPoint(request, mapPath);

        return new RouteMapResponse(
                routeResponse.destinationBuildingName(),
                routeResponse.selectedEntranceId(),
                routeResponse.totalDistanceMeters(),
                routeResponse.approachDistanceMeters(),
                routeResponse.graphDistanceMeters(),
                startPoint,
                mapPath
        );
    }

    /**
     * 요청에 좌표가 있으면 그 값을 지도 시작 마커로 사용하고,
     * 건물 기반 출발 endpoint처럼 좌표가 없으면 반환된 경로의 첫 번째 지점을 대신 사용한다.
     */
    private MapPoint resolveStartPoint(RouteRequest request, List<MapPoint> mapPath) {
        if (request.start().lon() != null && request.start().lat() != null) {
            return new MapPoint(
                    request.start().lat(),
                    request.start().lon(),
                    request.start().ele() == null ? 0.0 : request.start().ele()
            );
        }

        if (!mapPath.isEmpty()) {
            return mapPath.get(0);
        }

        return null;
    }
}
