package com.example.campus_navigation_backend.visualizer;

import com.example.campus_navigation_backend.application.CampusNavigationFacade;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 일반 경로 탐색 결과를 지도 시각화용 응답으로 변환하는 facade이다.
 */
@Service
@RequiredArgsConstructor
public class RouteMapFacade {

    private final CampusNavigationFacade campusNavigationFacade;
    private final CoordinateTransformService coordinateTransformService;

    /**
     * 최단 경로를 조회한 뒤 metric 좌표 경로를 WGS84 좌표 경로로 변환한다.
     *
     * @param request 경로 탐색 요청
     * @return 지도 시각화용 경로 응답
     */
    public RouteMapResponse findRouteForMap(RouteRequest request) {
        RouteResponse routeResponse = campusNavigationFacade.findRoute(request);

        List<MapPoint> mapPath = coordinateTransformService.metricRouteToWgs84(routeResponse.path());

        MapPoint startPoint = new MapPoint(
                request.latitude(),
                request.longitude(),
                request.altitude() == null ? 0.0 : request.altitude()
        );

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
}
