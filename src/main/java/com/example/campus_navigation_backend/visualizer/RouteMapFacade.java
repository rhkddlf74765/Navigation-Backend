package com.example.campus_navigation_backend.visualizer;

import com.example.campus_navigation_backend.application.CampusNavigationFacade;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteMapFacade {

    private final CampusNavigationFacade campusNavigationFacade;
    private final CoordinateTransformService coordinateTransformService;

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
