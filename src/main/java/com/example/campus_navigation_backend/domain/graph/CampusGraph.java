package com.example.campus_navigation_backend.domain.graph;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class CampusGraph {

    private final AtomicLong nodeSequence = new AtomicLong(1);
    private final Map<NodeKey, Long> baseNodeIndex = new HashMap<>();

    private final Map<Long, GraphNode> nodes = new HashMap<>();
    private final Map<Long, List<GraphEdge>> adjacency = new HashMap<>();
    private final Map<Long, LineEndpoints> lineEndpointsByLineId = new HashMap<>();
    private final Map<String, List<Long>> buildingToEntranceNodeIds = new HashMap<>();
    private final Set<Long> startCandidateNodeIds = new HashSet<>();

    public long getOrCreateBaseNode(Point3D point) {
        NodeKey key = NodeKey.from(point);
        Long existing = baseNodeIndex.get(key);
        if (existing != null) {
            return existing;
        }

        long nodeId = nodeSequence.getAndIncrement();
        GraphNode node = new GraphNode(nodeId, GraphNodeType.BASE, nodeId, point);
        nodes.put(nodeId, node);
        adjacency.put(nodeId, new ArrayList<>());
        baseNodeIndex.put(key, nodeId);
        startCandidateNodeIds.add(nodeId);
        return nodeId;
    }

    public long createEntranceNode(Point3D point, long entranceId, String buildingName) {
        long nodeId = nodeSequence.getAndIncrement();
        GraphNode node = new GraphNode(nodeId, GraphNodeType.ENTRANCE, entranceId, point);
        nodes.put(nodeId, node);
        adjacency.put(nodeId, new ArrayList<>());
        startCandidateNodeIds.add(nodeId);
        return nodeId;
    }

    public long createProjectionNode(Point3D point) {
        long nodeId = nodeSequence.getAndIncrement();
        GraphNode node = new GraphNode(nodeId, GraphNodeType.PROJECTION, nodeId, point);
        nodes.put(nodeId, node);
        adjacency.put(nodeId, new ArrayList<>());
        return nodeId;
    }

    public void addDirectedEdge(long fromNodeId, long toNodeId, double cost, String edgeType, List<Point3D> geometry) {
        adjacency.computeIfAbsent(fromNodeId, key -> new ArrayList<>())
                .add(new GraphEdge(fromNodeId, toNodeId, cost, edgeType, geometry));
    }

    public void putLineEndpoints(long lineId, LineEndpoints endpoints) {
        lineEndpointsByLineId.put(lineId, endpoints);
    }

    public LineEndpoints getLineEndpoints(long lineId) {
        return lineEndpointsByLineId.get(lineId);
    }

    public void addBuildingEntrance(String normalizedBuildingName, long entranceNodeId) {
        buildingToEntranceNodeIds
                .computeIfAbsent(normalizedBuildingName, key -> new ArrayList<>())
                .add(entranceNodeId);
    }

    public List<Long> findEntranceNodeIdsByBuildingName(String buildingName) {
        String normalized = normalize(buildingName);
        return buildingToEntranceNodeIds.getOrDefault(normalized, List.of());
    }

    public GraphNode getNode(long nodeId) {
        return nodes.get(nodeId);
    }

    public List<GraphEdge> getAdjacency(long nodeId) {
        return adjacency.getOrDefault(nodeId, List.of());
    }

    public Set<Long> getStartCandidateNodeIds() {
        return Collections.unmodifiableSet(startCandidateNodeIds);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
