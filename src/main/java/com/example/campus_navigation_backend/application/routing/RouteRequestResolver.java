package com.example.campus_navigation_backend.application.routing;

import com.example.campus_navigation_backend.application.dto.RouteEndpointRequest;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.domain.geo.CoordinateTransformer;
import com.example.campus_navigation_backend.domain.geo.GeoPoint;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.MetricPoint;
import com.example.campus_navigation_backend.domain.projection.EdgeProjection;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RouteRequestResolver {

    private final CampusGraphStore campusGraphStore;

    private final CoordinateTransformer
            coordinateTransformer;

    private final EdgeProjectionFinder
            edgeProjectionFinder;

    public RouteRequestResolver(
            CampusGraphStore campusGraphStore,
            CoordinateTransformer
                    coordinateTransformer,
            EdgeProjectionFinder
                    edgeProjectionFinder
    ) {
        this.campusGraphStore =
                campusGraphStore;

        this.coordinateTransformer =
                coordinateTransformer;

        this.edgeProjectionFinder =
                edgeProjectionFinder;
    }

    public ResolvedRouteRequest resolve(
            RouteRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Route request is required."
            );
        }

        if (request.start() == null) {
            throw new IllegalArgumentException(
                    "Start endpoint is required."
            );
        }

        if (request.destination() == null) {
            throw new IllegalArgumentException(
                    "Destination endpoint is required."
            );
        }

        return new ResolvedRouteRequest(
                resolveEndpoint(
                        request.start()
                ),
                resolveEndpoint(
                        request.destination()
                )
        );
    }

    private ResolvedRouteEndpoint
    resolveEndpoint(
            RouteEndpointRequest endpoint
    ) {
        if (endpoint.type() == null) {
            throw new IllegalArgumentException(
                    "Endpoint type is required."
            );
        }

        return switch (endpoint.type()) {
            case BUILDING ->
                    resolveBuilding(
                            endpoint
                    );

            case COORDINATE ->
                    resolveCoordinate(
                            endpoint
                    );
        };
    }

    private ResolvedRouteEndpoint
    resolveBuilding(
            RouteEndpointRequest endpoint
    ) {
        if (endpoint.buildingName() == null
                || endpoint
                .buildingName()
                .isBlank()) {
            throw new IllegalArgumentException(
                    "Building name is required."
            );
        }

        List<Long> entranceNodeIds =
                campusGraphStore
                        .findEntranceNodeIdsByBuildingName(
                                endpoint.buildingName()
                        );

        if (entranceNodeIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "No building entrance found: "
                            + endpoint.buildingName()
            );
        }

        return new ResolvedRouteEndpoint
                .Building(
                endpoint
                        .buildingName()
                        .trim(),

                entranceNodeIds
        );
    }

    private ResolvedRouteEndpoint
    resolveCoordinate(
            RouteEndpointRequest endpoint
    ) {
        if (endpoint.lon() == null
                || endpoint.lat() == null) {
            throw new IllegalArgumentException(
                    "Lon and lat are required."
            );
        }

        GeoPoint wgs84 =
                new GeoPoint(
                        endpoint.lon(),
                        endpoint.lat(),
                        endpoint.ele() == null
                                ? 0.0
                                : endpoint.ele()
                );

        MetricPoint metricPoint =
                coordinateTransformer
                        .toMetric(
                                wgs84
                        );

        List<EdgeProjection> projections =
                edgeProjectionFinder
                        .findCandidates(
                                metricPoint
                        );

        return new ResolvedRouteEndpoint
                .Coordinate(
                metricPoint,
                projections
        );
    }
}