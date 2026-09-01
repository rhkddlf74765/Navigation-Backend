package com.example.campus_navigation_backend.application.routing;

import com.example.campus_navigation_backend.application.dto.RouteExpectedTime;
import com.example.campus_navigation_backend.application.dto.RoutePoint;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.domain.geo.CoordinateTransformer;
import com.example.campus_navigation_backend.domain.geo.GeoPoint;
import com.example.campus_navigation_backend.domain.path.PathFinder;
import com.example.campus_navigation_backend.domain.path.PathResult;
import com.example.campus_navigation_backend.domain.routing.RoutingOverlay;
import com.example.campus_navigation_backend.log.service.RouteLogService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CampusNavigationFacade {

    private final RouteRequestResolver
            routeRequestResolver;

    private final RoutingOverlayFactory
            routingOverlayFactory;

    private final PathFinder pathFinder;

    private final CoordinateTransformer
            coordinateTransformer;

    private final RouteLogService
            routeLogService;

    public CampusNavigationFacade(
            RouteRequestResolver
                    routeRequestResolver,
            RoutingOverlayFactory
                    routingOverlayFactory,
            PathFinder pathFinder,
            CoordinateTransformer
                    coordinateTransformer,
            RouteLogService
                    routeLogService
    ) {
        this.routeRequestResolver =
                routeRequestResolver;

        this.routingOverlayFactory =
                routingOverlayFactory;

        this.pathFinder =
                pathFinder;

        this.coordinateTransformer =
                coordinateTransformer;

        this.routeLogService =
                routeLogService;
    }

    public RouteResponse findRoute(
            RouteRequest request
    ) {

        UUID routeSessionId =
                UUID.randomUUID();

        Instant requestedAt =
                Instant.now();

        routeLogService.saveRouteRequested(
                routeSessionId,
                request,
                requestedAt
        );

        try {

            ResolvedRouteRequest resolvedRequest =
                    routeRequestResolver.resolve(
                            request
                    );

            RoutingOverlay overlay =
                    routingOverlayFactory.create(
                            resolvedRequest
                    );

            PathResult result =
                    pathFinder.findPath(
                            overlay,
                            overlay.startNodeId(),
                            overlay.goalNodeId()
                    );

            if (!result.found()) {
                throw new IllegalStateException(
                        "Route not found."
                );
            }

            List<RoutePoint> routePoints =
                    result.pathPoints()
                            .stream()
                            .map(
                                    coordinateTransformer::toWgs84
                            )
                            .map(
                                    point ->
                                            new RoutePoint(
                                                    point.lon(),
                                                    point.lat(),
                                                    point.ele()
                                            )
                            )
                            .toList();

            double totalDistanceMeters =
                    result.totalDistanceMeters();

            double totalCost =
                    result.totalCost();

            RouteExpectedTime expectedTime =
                    RouteExpectedTime.fromDistance(
                            totalDistanceMeters
                    );

            Instant respondedAt =
                    Instant.now();

            routeLogService.saveRouteReturned(
                    routeSessionId,
                    totalDistanceMeters,
                    totalCost,
                    expectedTime.seconds(),
                    routePoints,
                    respondedAt
            );

            return new RouteResponse(
                    routeSessionId,
                    totalDistanceMeters,
                    expectedTime,
                    routePoints
            );

        } catch (RuntimeException exception) {

            routeLogService.saveRouteFailed(
                    routeSessionId,
                    Instant.now(),
                    exception
            );

            throw exception;
        }
    }

    private RouteResponse
    zeroRouteIfSameCoordinate(
            UUID routeSessionId,
            ResolvedRouteRequest request
    ) {
        if (!(request.start()
                instanceof
                ResolvedRouteEndpoint
                        .Coordinate start)

                || !(request.destination()
                instanceof
                ResolvedRouteEndpoint
                        .Coordinate destination)) {

            return null;
        }

        if (start.point()
                .distance2D(
                        destination.point()
                ) > 1e-6) {
            return null;
        }

        GeoPoint point =
                coordinateTransformer
                        .toWgs84(
                                start.point()
                        );

        return new RouteResponse(
                routeSessionId,
                0.0,
                RouteExpectedTime
                        .fromDistance(
                                0.0
                        ),
                List.of(
                        toRoutePoint(point)
                )
        );
    }

    private RouteResponse toResponse(
            UUID routeSessionId,
            ResolvedRouteRequest request,
            PathResult result
    ) {
        List<RoutePoint> path =
                result.pathPoints()
                        .stream()
                        .map(
                                coordinateTransformer
                                        ::toWgs84
                        )
                        .map(
                                this::toRoutePoint
                        )
                        .toList();

        return new RouteResponse(
                routeSessionId,
                result.totalDistanceMeters(),
                RouteExpectedTime.fromDistance(
                        result.totalDistanceMeters()
                ),
                path
        );
    }

    private RoutePoint toRoutePoint(
            GeoPoint point
    ) {
        return new RoutePoint(
                point.lon(),
                point.lat(),
                point.ele()
        );
    }
}
