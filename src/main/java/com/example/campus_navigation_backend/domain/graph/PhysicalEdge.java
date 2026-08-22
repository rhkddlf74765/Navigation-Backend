package com.example.campus_navigation_backend.domain.graph;

import com.example.campus_navigation_backend.domain.graph.cost.EdgeCostPolicy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PhysicalEdge {

    private static final double EPS = 1e-9;

    private final long id;
    private final long sourceNodeId;
    private final long targetNodeId;
    private final double distanceMeters;
    private final String highway;
    private final List<MetricPoint> geometry;

    private final double[] cumulativeDistance;
    private final double[] cumulativeForwardCost;
    private final double[] cumulativeReverseCost;

    private PhysicalEdge(
            long id,
            long sourceNodeId,
            long targetNodeId,
            double distanceMeters,
            String highway,
            List<MetricPoint> geometry,
            double[] cumulativeDistance,
            double[] cumulativeForwardCost,
            double[] cumulativeReverseCost
    ) {
        this.id = id;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.distanceMeters = distanceMeters;
        this.highway = highway;

        this.geometry =
                List.copyOf(geometry);

        this.cumulativeDistance =
                cumulativeDistance.clone();

        this.cumulativeForwardCost =
                cumulativeForwardCost.clone();

        this.cumulativeReverseCost =
                cumulativeReverseCost.clone();
    }

    public static PhysicalEdge create(
            long id,
            long sourceNodeId,
            long targetNodeId,
            double distanceMeters,
            String highway,
            List<MetricPoint> geometry,
            EdgeCostPolicy edgeCostPolicy
    ) {
        if (!Double.isFinite(distanceMeters)
                || distanceMeters <= 0.0) {
            throw new IllegalArgumentException(
                    "Edge distance must be positive. edgeId="
                            + id
            );
        }

        if (geometry == null
                || geometry.size() < 2) {
            throw new IllegalArgumentException(
                    "Edge geometry must contain at least two points. edgeId="
                            + id
            );
        }

        List<MetricPoint> immutableGeometry =
                List.copyOf(geometry);

        int segmentCount =
                immutableGeometry.size() - 1;

        double[] metricLengths =
                new double[segmentCount];

        double totalMetricLength = 0.0;

        for (int i = 0;
             i < segmentCount;
             i++) {

            double length =
                    immutableGeometry
                            .get(i)
                            .distance2D(
                                    immutableGeometry
                                            .get(i + 1)
                            );

            metricLengths[i] = length;
            totalMetricLength += length;
        }

        if (totalMetricLength <= EPS) {
            throw new IllegalArgumentException(
                    "Edge geometry has zero 2D length. edgeId="
                            + id
            );
        }

        double[] cumulativeDistance =
                new double[segmentCount + 1];

        double[] cumulativeForwardCost =
                new double[segmentCount + 1];

        double[] cumulativeReverseCost =
                new double[segmentCount + 1];

        for (int i = 0;
             i < segmentCount;
             i++) {

            MetricPoint from =
                    immutableGeometry.get(i);

            MetricPoint to =
                    immutableGeometry.get(i + 1);

            double segmentDistance =
                    distanceMeters
                            * (
                            metricLengths[i]
                                    / totalMetricLength
                    );

            double elevationDelta =
                    to.z() - from.z();

            double forwardCost =
                    edgeCostPolicy.calculate(
                            highway,
                            segmentDistance,
                            elevationDelta
                    );

            double reverseCost =
                    edgeCostPolicy.calculate(
                            highway,
                            segmentDistance,
                            -elevationDelta
                    );

            cumulativeDistance[i + 1] =
                    cumulativeDistance[i]
                            + segmentDistance;

            cumulativeForwardCost[i + 1] =
                    cumulativeForwardCost[i]
                            + forwardCost;

            cumulativeReverseCost[i + 1] =
                    cumulativeReverseCost[i]
                            + reverseCost;
        }

        cumulativeDistance[segmentCount] =
                distanceMeters;

        return new PhysicalEdge(
                id,
                sourceNodeId,
                targetNodeId,
                distanceMeters,
                highway,
                immutableGeometry,
                cumulativeDistance,
                cumulativeForwardCost,
                cumulativeReverseCost
        );
    }

    public long id() {
        return id;
    }

    public long sourceNodeId() {
        return sourceNodeId;
    }

    public long targetNodeId() {
        return targetNodeId;
    }

    public double distanceMeters() {
        return distanceMeters;
    }

    public String highway() {
        return highway;
    }

    public List<MetricPoint> geometry() {
        return geometry;
    }

    public double forwardCost() {
        return cumulativeForwardCost[
                cumulativeForwardCost.length - 1
                ];
    }

    public double reverseCost() {
        return cumulativeReverseCost[
                cumulativeReverseCost.length - 1
                ];
    }

    public EdgePosition sourcePosition() {
        return new EdgePosition(
                0,
                0.0,
                0.0
        );
    }

    public EdgePosition targetPosition() {
        return new EdgePosition(
                geometry.size() - 2,
                1.0,
                distanceMeters
        );
    }

    public EdgePosition position(
            int segmentIndex,
            double fraction
    ) {
        if (segmentIndex < 0
                || segmentIndex
                >= geometry.size() - 1) {
            throw new IllegalArgumentException(
                    "Invalid segmentIndex="
                            + segmentIndex
                            + ", edgeId="
                            + id
            );
        }

        double segmentDistance =
                cumulativeDistance[segmentIndex + 1]
                        - cumulativeDistance[segmentIndex];

        double offset =
                cumulativeDistance[segmentIndex]
                        + segmentDistance * fraction;

        return new EdgePosition(
                segmentIndex,
                fraction,
                offset
        );
    }

    public double distanceBetween(
            EdgePosition left,
            EdgePosition right
    ) {
        return Math.abs(
                right.offsetMetersFromSource()
                        - left.offsetMetersFromSource()
        );
    }

    public double forwardCostBetween(
            EdgePosition from,
            EdgePosition to
    ) {
        if (from.offsetMetersFromSource()
                > to.offsetMetersFromSource()
                + EPS) {
            throw new IllegalArgumentException(
                    "Forward range is reversed."
            );
        }

        return forwardCostAt(to)
                - forwardCostAt(from);
    }

    public double reverseCostBetween(
            EdgePosition from,
            EdgePosition to
    ) {
        if (from.offsetMetersFromSource()
                + EPS
                < to.offsetMetersFromSource()) {
            throw new IllegalArgumentException(
                    "Reverse range is reversed."
            );
        }

        return reverseCostAt(from)
                - reverseCostAt(to);
    }

    public List<MetricPoint> slice(
            EdgePosition from,
            EdgePosition to
    ) {
        if (from.offsetMetersFromSource()
                <= to.offsetMetersFromSource()) {
            return sliceForward(
                    from,
                    to
            );
        }

        List<MetricPoint> reversed =
                new ArrayList<>(
                        sliceForward(to, from)
                );

        Collections.reverse(reversed);

        return List.copyOf(reversed);
    }

    private double forwardCostAt(
            EdgePosition position
    ) {
        int index =
                position.segmentIndex();

        double segmentCost =
                cumulativeForwardCost[index + 1]
                        - cumulativeForwardCost[index];

        return cumulativeForwardCost[index]
                + segmentCost
                * position.fraction();
    }

    private double reverseCostAt(
            EdgePosition position
    ) {
        int index =
                position.segmentIndex();

        double segmentCost =
                cumulativeReverseCost[index + 1]
                        - cumulativeReverseCost[index];

        return cumulativeReverseCost[index]
                + segmentCost
                * position.fraction();
    }

    private List<MetricPoint> sliceForward(
            EdgePosition from,
            EdgePosition to
    ) {
        List<MetricPoint> path =
                new ArrayList<>();

        appendIfNeeded(
                path,
                interpolate(from)
        );

        for (int vertexIndex =
             from.segmentIndex() + 1;
             vertexIndex
                     <= to.segmentIndex();
             vertexIndex++) {

            appendIfNeeded(
                    path,
                    geometry.get(vertexIndex)
            );
        }

        appendIfNeeded(
                path,
                interpolate(to)
        );

        return List.copyOf(path);
    }

    private MetricPoint interpolate(
            EdgePosition position
    ) {
        MetricPoint start =
                geometry.get(
                        position.segmentIndex()
                );

        MetricPoint end =
                geometry.get(
                        position.segmentIndex() + 1
                );

        double fraction =
                position.fraction();

        return new MetricPoint(
                start.x()
                        + (
                        end.x() - start.x()
                ) * fraction,

                start.y()
                        + (
                        end.y() - start.y()
                ) * fraction,

                start.z()
                        + (
                        end.z() - start.z()
                ) * fraction
        );
    }

    private void appendIfNeeded(
            List<MetricPoint> path,
            MetricPoint point
    ) {
        if (path.isEmpty()
                || path
                .get(path.size() - 1)
                .distance3D(point)
                > EPS) {
            path.add(point);
        }
    }
}
