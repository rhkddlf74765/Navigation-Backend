package com.example.campus_navigation_backend.infrastructure.spatial;

import com.example.campus_navigation_backend.config.NavigationProperties;
import com.example.campus_navigation_backend.domain.graph.MetricPoint;
import com.example.campus_navigation_backend.domain.graph.PhysicalEdge;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class EdgeSpatialIndex {

    private final NavigationProperties properties;

    private final AtomicReference<
            Map<Cell, List<PhysicalEdge>>>
            cellsReference =
            new AtomicReference<>();

    public EdgeSpatialIndex(
            NavigationProperties properties
    ) {
        this.properties = properties;
    }

    public void initialize(
            Collection<PhysicalEdge> edges
    ) {
        double cellSize =
                properties
                        .spatialIndexCellSizeMeters();

        if (cellSize <= 0.0) {
            throw new IllegalStateException(
                    "Spatial index cell size must be positive."
            );
        }

        Map<Cell, List<PhysicalEdge>>
                mutable =
                new HashMap<>();

        for (PhysicalEdge edge : edges) {
            Envelope envelope =
                    envelope(
                            edge.geometry()
                    );

            int minX =
                    cell(
                            envelope.minX(),
                            cellSize
                    );

            int maxX =
                    cell(
                            envelope.maxX(),
                            cellSize
                    );

            int minY =
                    cell(
                            envelope.minY(),
                            cellSize
                    );

            int maxY =
                    cell(
                            envelope.maxY(),
                            cellSize
                    );

            for (int x = minX;
                 x <= maxX;
                 x++) {

                for (int y = minY;
                     y <= maxY;
                     y++) {

                    mutable
                            .computeIfAbsent(
                                    new Cell(
                                            x,
                                            y
                                    ),
                                    ignored ->
                                            new ArrayList<>()
                            )
                            .add(edge);
                }
            }
        }

        Map<Cell, List<PhysicalEdge>>
                immutable =
                new HashMap<>();

        mutable.forEach(
                (cell, list) ->
                        immutable.put(
                                cell,
                                List.copyOf(list)
                        )
        );

        if (!cellsReference.compareAndSet(
                null,
                Map.copyOf(immutable)
        )) {
            throw new IllegalStateException(
                    "EdgeSpatialIndex has already been initialized."
            );
        }
    }

    public List<PhysicalEdge> query(
            MetricPoint point,
            double radiusMeters
    ) {
        Map<Cell, List<PhysicalEdge>>
                cells =
                cells();

        double cellSize =
                properties
                        .spatialIndexCellSizeMeters();

        int minX =
                cell(
                        point.x()
                                - radiusMeters,
                        cellSize
                );

        int maxX =
                cell(
                        point.x()
                                + radiusMeters,
                        cellSize
                );

        int minY =
                cell(
                        point.y()
                                - radiusMeters,
                        cellSize
                );

        int maxY =
                cell(
                        point.y()
                                + radiusMeters,
                        cellSize
                );

        Map<Long, PhysicalEdge>
                unique =
                new LinkedHashMap<>();

        for (int x = minX;
             x <= maxX;
             x++) {

            for (int y = minY;
                 y <= maxY;
                 y++) {

                for (PhysicalEdge edge
                        : cells.getOrDefault(
                        new Cell(x, y),
                        List.of()
                )) {

                    unique.putIfAbsent(
                            edge.id(),
                            edge
                    );
                }
            }
        }

        return List.copyOf(
                unique.values()
        );
    }

    private Map<Cell, List<PhysicalEdge>>
    cells() {
        Map<Cell, List<PhysicalEdge>>
                cells =
                cellsReference.get();

        if (cells == null) {
            throw new IllegalStateException(
                    "EdgeSpatialIndex has not been initialized."
            );
        }

        return cells;
    }

    private int cell(
            double coordinate,
            double cellSize
    ) {
        return (int) Math.floor(
                coordinate / cellSize
        );
    }

    private Envelope envelope(
            List<MetricPoint> geometry
    ) {
        double minX =
                Double.POSITIVE_INFINITY;

        double minY =
                Double.POSITIVE_INFINITY;

        double maxX =
                Double.NEGATIVE_INFINITY;

        double maxY =
                Double.NEGATIVE_INFINITY;

        for (MetricPoint point : geometry) {
            minX =
                    Math.min(
                            minX,
                            point.x()
                    );

            minY =
                    Math.min(
                            minY,
                            point.y()
                    );

            maxX =
                    Math.max(
                            maxX,
                            point.x()
                    );

            maxY =
                    Math.max(
                            maxY,
                            point.y()
                    );
        }

        return new Envelope(
                minX,
                minY,
                maxX,
                maxY
        );
    }

    private record Cell(
            int x,
            int y
    ) {
    }

    private record Envelope(
            double minX,
            double minY,
            double maxX,
            double maxY
    ) {
    }
}
