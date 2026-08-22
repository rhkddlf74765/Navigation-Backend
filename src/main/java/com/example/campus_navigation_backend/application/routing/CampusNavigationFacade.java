package com.example.campus_navigation_backend.application.routing;

import com.example.campus_navigation_backend.application.dto.RouteExpectedTime;
import com.example.campus_navigation_backend.application.dto.RoutePoint;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.domain.geo.CoordinateTransformer;
import com.example.campus_navigation_backend.domain.geo.GeoPoint;
import com.example.campus_navigation_backend.domain.graph.RoutingArc;
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

        try {
            ResolvedRouteRequest
                    resolvedRequest =
                    routeRequestResolver
                            .resolve(
                                    request
                            );

            RouteResponse zeroRoute =
                    zeroRouteIfSameCoordinate(
                            routeSessionId,
                            resolvedRequest
                    );

            if (zeroRoute != null) {
                routeLogService
                        .saveRouteReturned(
                                request,
                                zeroRoute,
                                requestedAt,
                                Instant.now()
                        );

                return zeroRoute;
            }

            RoutingOverlay overlay =
                    routingOverlayFactory
                            .create(
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
                        "No reachable route found."
                );
            }

            RouteResponse response =
                    toResponse(
                            routeSessionId,
                            resolvedRequest,
                            result
                    );

            routeLogService
                    .saveRouteReturned(
                            request,
                            response,
                            requestedAt,
                            Instant.now()
                    );

            return response;

        } catch (RuntimeException exception) {

            routeLogService
                    .saveRouteFailed(
                            request,
                            routeSessionId,
                            requestedAt,
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
                null,
                null,
                0.0,
                0.0,
                RouteExpectedTime
                        .fromDistance(
                                0.0
                        ),
                0.0,
                0.0,
                0.0,
                0.0,
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
        Long selectedEntranceId =
                selectedDestinationEntranceId(
                        request.destination(),
                        result
                );

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

                request
                        .destination()
                        .displayName(),

                selectedEntranceId,

                result
                        .totalDistanceMeters(),

                result.totalCost(),

                RouteExpectedTime
                        .fromDistance(
                                result
                                        .totalDistanceMeters()
                        ),

                result
                        .approachDistanceMeters(),

                result.approachCost(),

                result
                        .graphDistanceMeters(),

                result.graphCost(),

                path
        );
    }

    private Long
    selectedDestinationEntranceId(
            ResolvedRouteEndpoint destination,
            PathResult result
    ) {
        if (!(destination
                instanceof
                ResolvedRouteEndpoint
                        .Building building)) {
            return null;
        }

        for (int i =
             result.arcs().size() - 1;
             i >= 0;
             i--) {

            RoutingArc arc =
                    result.arcs().get(i);

            if (building
                    .entranceNodeIds()
                    .contains(
                            arc.fromNodeId()
                    )) {

                return arc.fromNodeId();
            }
        }

        return null;
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