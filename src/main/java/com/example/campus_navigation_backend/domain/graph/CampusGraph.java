package com.example.campus_navigation_backend.domain.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CampusGraph
        implements RoutingGraph {

    private final Map<Long, GraphNode> nodes;

    private final Map<Long, List<GraphEdge>>
            adjacency;

    private final Map<Long, PhysicalEdge>
            physicalEdges;

    private final Map<Long, BuildingEntrances>
            buildingsById;

    private final Map<String, Long>
            buildingIdsByName;

    private final double heuristicCostPerMeter;

    private CampusGraph(
            Builder builder
    ) {
        this.nodes =
                Map.copyOf(builder.nodes);

        this.adjacency =
                copyAdjacency(
                        builder.adjacency
                );

        this.physicalEdges =
                Map.copyOf(
                        builder.physicalEdges
                );

        this.buildingsById =
                copyBuildings(
                        builder.buildingsById
                );

        this.buildingIdsByName =
                Map.copyOf(
                        builder.buildingIdsByName
                );

        this.heuristicCostPerMeter =
                calculateHeuristicCostPerMeter(
                        nodes,
                        adjacency
                );
    }

    public static Builder builder() {
        return new Builder();
    }

    public GraphNode getNode(
            long nodeId
    ) {
        return nodes.get(nodeId);
    }

    @Override
    public boolean containsNode(
            long nodeId
    ) {
        return nodes.containsKey(
                nodeId
        );
    }

    @Override
    public List<? extends RoutingArc>
    getAdjacency(
            long nodeId
    ) {
        return adjacency.getOrDefault(
                nodeId,
                List.of()
        );
    }

    public List<GraphEdge>
    getBaseAdjacency(
            long nodeId
    ) {
        return adjacency.getOrDefault(
                nodeId,
                List.of()
        );
    }

    public List<GraphNode> getNodes() {
        return List.copyOf(
                nodes.values()
        );
    }

    public List<GraphEdge> getEdges() {
        return adjacency.values()
                .stream()
                .flatMap(List::stream)
                .toList();
    }

    public List<PhysicalEdge>
    getPhysicalEdges() {
        return List.copyOf(
                physicalEdges.values()
        );
    }

    public PhysicalEdge getPhysicalEdge(
            long edgeId
    ) {
        return physicalEdges.get(
                edgeId
        );
    }

    public List<Long>
    findEntranceNodeIdsByBuildingId(
            long buildingId
    ) {
        BuildingEntrances entry =
                buildingsById.get(
                        buildingId
                );

        return entry == null
                ? List.of()
                : entry.nodeIds();
    }

    public List<Long>
    findEntranceNodeIdsByBuildingName(
            String buildingName
    ) {
        Long buildingId =
                buildingIdsByName.get(
                        normalize(buildingName)
                );

        if (buildingId == null) {
            return List.of();
        }

        BuildingEntrances entry =
                buildingsById.get(
                        buildingId
                );

        return entry == null
                ? List.of()
                : entry.nodeIds();
    }

    public List<String> findBuildingNames() {
        return buildingsById.values()
                .stream()
                .map(
                        BuildingEntrances::displayName
                )
                .distinct()
                .sorted()
                .toList();
    }

    public double heuristicCostPerMeter() {
        return heuristicCostPerMeter;
    }

    @Override
    public double estimateMinimumCost(
            long fromNodeId,
            long goalNodeId
    ) {
        GraphNode from =
                nodes.get(fromNodeId);

        GraphNode goal =
                nodes.get(goalNodeId);

        if (from == null
                || goal == null) {
            return 0.0;
        }

        return from
                .point()
                .distance2D(
                        goal.point()
                )
                * heuristicCostPerMeter;
    }

    private static double
    calculateHeuristicCostPerMeter(
            Map<Long, GraphNode> nodes,
            Map<Long, List<GraphEdge>>
                    adjacency
    ) {
        double minimumRatio =
                Double.POSITIVE_INFINITY;

        for (List<GraphEdge> edges
                : adjacency.values()) {

            for (GraphEdge edge : edges) {

                GraphNode from =
                        nodes.get(
                                edge.fromNodeId()
                        );

                GraphNode to =
                        nodes.get(
                                edge.toNodeId()
                        );

                double straightDistance =
                        from.point()
                                .distance2D(
                                        to.point()
                                );

                if (straightDistance <= 0.0) {
                    continue;
                }

                minimumRatio =
                        Math.min(
                                minimumRatio,
                                edge.cost()
                                        / straightDistance
                        );
            }
        }

        return Double.isFinite(
                minimumRatio
        )
                ? Math.max(
                0.0,
                minimumRatio
        )
                : 0.0;
    }

    private static Map<Long, List<GraphEdge>>
    copyAdjacency(
            Map<Long, List<GraphEdge>> source
    ) {
        Map<Long, List<GraphEdge>> copied =
                new HashMap<>();

        source.forEach(
                (nodeId, edges) ->
                        copied.put(
                                nodeId,
                                List.copyOf(edges)
                        )
        );

        return Map.copyOf(copied);
    }

    private static Map<Long, BuildingEntrances>
    copyBuildings(
            Map<Long,
                    MutableBuildingEntrances>
                    source
    ) {
        Map<Long, BuildingEntrances> copied =
                new LinkedHashMap<>();

        source.forEach(
                (key, value) ->
                        copied.put(
                                key,
                                new BuildingEntrances(
                                        value.displayName,
                                        List.copyOf(
                                                value.nodeIds
                                        )
                                )
                        )
        );

        return Map.copyOf(copied);
    }

    private static String normalize(
            String value
    ) {
        return value == null
                ? ""
                : value
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private record BuildingEntrances(
            String displayName,
            List<Long> nodeIds
    ) {
    }

    private static final class
    MutableBuildingEntrances {

        private final String displayName;
        private final List<Long> nodeIds =
                new ArrayList<>();

        private MutableBuildingEntrances(
                String displayName
        ) {
            this.displayName =
                    displayName;
        }
    }

    public static final class Builder {

        private final Map<Long, GraphNode>
                nodes =
                new HashMap<>();

        private final
        Map<Long, List<GraphEdge>>
                adjacency =
                new HashMap<>();

        private final Map<Long, PhysicalEdge>
                physicalEdges =
                new LinkedHashMap<>();

        private final
        Map<Long,
                MutableBuildingEntrances>
                buildingsById =
                new LinkedHashMap<>();

        private final Map<String, Long>
                buildingIdsByName =
                new LinkedHashMap<>();

        public void addNode(
                long nodeId,
                GraphNodeType type,
                MetricPoint point
        ) {
            if (nodeId < 0) {
                throw new IllegalArgumentException(
                        "Base graph node IDs must be non-negative."
                );
            }

            if (nodes.putIfAbsent(
                    nodeId,
                    new GraphNode(
                            nodeId,
                            type,
                            point
                    )
            ) != null) {
                throw new IllegalStateException(
                        "Duplicate graph node. nodeId="
                                + nodeId
                );
            }

            adjacency.put(
                    nodeId,
                    new ArrayList<>()
            );
        }

        public GraphNode getNode(
                long nodeId
        ) {
            return nodes.get(nodeId);
        }

        public void addBuildingEntrance(
                long buildingId,
                String displayName,
                long nodeId
        ) {
            if (!nodes.containsKey(nodeId)) {
                throw new IllegalArgumentException(
                        "Building entrance node does not exist."
                );
            }

            String normalized =
                    normalize(displayName);

            if (normalized.isBlank()) {
                return;
            }

            MutableBuildingEntrances entry =
                    buildingsById.computeIfAbsent(
                            buildingId,
                            ignored ->
                                    new MutableBuildingEntrances(
                                            displayName.trim()
                                    )
                    );

            if (!entry.nodeIds.contains(
                    nodeId
            )) {
                entry.nodeIds.add(
                        nodeId
                );
            }

            buildingIdsByName.putIfAbsent(
                    normalized,
                    buildingId
            );
        }

        public void addPhysicalEdge(
                PhysicalEdge edge
        ) {
            if (!nodes.containsKey(
                    edge.sourceNodeId()
            )
                    || !nodes.containsKey(
                    edge.targetNodeId()
            )) {
                throw new IllegalStateException(
                        "Physical edge endpoint node missing."
                );
            }

            if (physicalEdges.putIfAbsent(
                    edge.id(),
                    edge
            ) != null) {
                throw new IllegalStateException(
                        "Duplicate physical edge. edgeId="
                                + edge.id()
                );
            }

            GraphEdge forward =
                    new GraphEdge(
                            edge.id(),
                            edge.sourceNodeId(),
                            edge.targetNodeId(),
                            edge.distanceMeters(),
                            edge.forwardCost(),
                            edge.highway(),
                            edge.geometry()
                    );

            List<MetricPoint> reverseGeometry =
                    new ArrayList<>(
                            edge.geometry()
                    );

            java.util.Collections
                    .reverse(
                            reverseGeometry
                    );

            GraphEdge reverse =
                    new GraphEdge(
                            edge.id(),
                            edge.targetNodeId(),
                            edge.sourceNodeId(),
                            edge.distanceMeters(),
                            edge.reverseCost(),
                            edge.highway(),
                            reverseGeometry
                    );

            adjacency
                    .get(
                            edge.sourceNodeId()
                    )
                    .add(forward);

            adjacency
                    .get(
                            edge.targetNodeId()
                    )
                    .add(reverse);
        }

        public CampusGraph build() {
            if (nodes.isEmpty()) {
                throw new IllegalStateException(
                        "CampusGraph cannot be empty."
                );
            }

            return new CampusGraph(this);
        }
    }
}
