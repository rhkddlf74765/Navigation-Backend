package com.example.campus_navigation_backend.api.graphmap;

import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapPointRequest;
import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapPointResponse;
import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapProjectionResponse;
import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapRouteResultResponse;
import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapRouteSessionResponse;
import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapRouteStartRequest;
import com.example.campus_navigation_backend.api.graphmap.dto.GraphMapRouteStepResponse;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Debug API service that exposes point projection and polling-based A* progress.
 */
@Service
@RequiredArgsConstructor
public class GraphMapDebugService {

    private static final long SNAPSHOT_INTERVAL_MILLIS = 500L;
    private static final int SNAPSHOT_LIMIT = 80;
    private static final double EPS = 1e-9;

    private final CampusGraphStore campusGraphStore;

    private final ExecutorService executorService = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "graph-map-debug");
        thread.setDaemon(true);
        return thread;
    });

    private final Map<UUID, RouteSessionState> sessions = new ConcurrentHashMap<>();

    @PreDestroy
    void shutdown() {
        executorService.shutdownNow();
    }

    public GraphMapProjectionResponse project(GraphMapPointRequest request) {
        ProjectionCandidate candidate = projectToNearestEdge(request.toPoint3D());
        return candidate.toResponse(request);
    }

    public GraphMapRouteSessionResponse startRoute(GraphMapRouteStartRequest request) {
        UUID sessionId = UUID.randomUUID();
        RouteSessionState session = new RouteSessionState(sessionId);
        sessions.put(sessionId, session);

        CompletableFuture.runAsync(() -> runSearch(session, request), executorService);
        return session.snapshot();
    }

    public GraphMapRouteSessionResponse getSession(UUID sessionId) {
        RouteSessionState session = findSession(sessionId);
        return session.snapshot();
    }

    private void runSearch(RouteSessionState session, GraphMapRouteStartRequest request) {
        try {
            ProjectionCandidate startProjection = projectToNearestEdge(request.startPoint().toPoint3D());
            ProjectionCandidate destinationProjection = projectToNearestEdge(request.destinationPoint().toPoint3D());

            session.start(startProjection.toResponse(request.startPoint()), destinationProjection.toResponse(request.destinationPoint()));

            RouteSearchGraph graph = RouteSearchGraph.from(campusGraphStore.getEdges());
            RouteCandidate bestRoute = findBestRoute(graph, startProjection, destinationProjection, session);

            if (bestRoute != null) {
                session.complete(bestRoute.searchResult().toResponse());
            } else {
                session.fail("No reachable route found.");
            }
        } catch (Exception ex) {
            session.fail(ex.getMessage() == null ? "Unexpected route debug failure." : ex.getMessage());
        }
    }

    private RouteCandidate findBestRoute(RouteSearchGraph graph,
                                         ProjectionCandidate startProjection,
                                         ProjectionCandidate destinationProjection,
                                         RouteSessionState session) {
        RouteCandidate bestCandidate = null;

        for (ProjectionOption startOption : startProjection.options()) {
            for (ProjectionOption destinationOption : destinationProjection.options()) {
                AStarSearchResult searchResult = search(graph, startOption.endpointNodeId(), destinationOption.endpointNodeId(), session);
                if (!searchResult.found()) {
                    continue;
                }

                double totalCost =
                        startProjection.inputToProjectionCost()
                                + startOption.connectorCost()
                                + searchResult.totalCost()
                                + destinationOption.connectorCost()
                                + destinationProjection.inputToProjectionCost();

                if (bestCandidate == null || totalCost < bestCandidate.totalCost()) {
                    bestCandidate = new RouteCandidate(
                            startOption.endpointNodeId(),
                            destinationOption.endpointNodeId(),
                            totalCost,
                            searchResult
                    );
                }
            }
        }

        return bestCandidate;
    }

    private AStarSearchResult search(RouteSearchGraph graph, long startNodeId, long goalNodeId, RouteSessionState session) {
        PriorityQueue<SearchNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(SearchNode::score));
        Map<Long, Double> gScore = new HashMap<>();
        Map<Long, Long> cameFrom = new HashMap<>();
        Map<Long, Double> cameCost = new HashMap<>();
        Set<Long> closedSet = new HashSet<>();
        Deque<Long> recentVisited = new ArrayDeque<>();

        gScore.put(startNodeId, 0.0);
        openSet.add(new SearchNode(startNodeId, 0.0));

        long lastSnapshotAt = System.currentTimeMillis();

        while (!openSet.isEmpty()) {
            SearchNode current = openSet.poll();
            if (!closedSet.add(current.nodeId())) {
                continue;
            }

            recentVisited.addLast(current.nodeId());
            while (recentVisited.size() > SNAPSHOT_LIMIT) {
                recentVisited.removeFirst();
            }

            session.updateProgress(
                    closedSet.size(),
                    openSet.size(),
                    current.nodeId(),
                    new ArrayList<>(recentVisited),
                    snapshotFrontier(openSet)
            );

            if (current.nodeId() == goalNodeId) {
                return AStarSearchResult.found(
                        reconstructPath(goalNodeId, cameFrom),
                        cameCost,
                        gScore.getOrDefault(goalNodeId, 0.0)
                );
            }

            for (SearchEdge neighbor : graph.neighbors(current.nodeId())) {
                double tentativeG = gScore.getOrDefault(current.nodeId(), Double.POSITIVE_INFINITY) + neighbor.cost();
                double knownG = gScore.getOrDefault(neighbor.toNodeId(), Double.POSITIVE_INFINITY);
                if (tentativeG + EPS >= knownG) {
                    continue;
                }

                cameFrom.put(neighbor.toNodeId(), current.nodeId());
                cameCost.put(neighbor.toNodeId(), neighbor.cost());
                gScore.put(neighbor.toNodeId(), tentativeG);
                openSet.add(new SearchNode(neighbor.toNodeId(), tentativeG));
            }

            long now = System.currentTimeMillis();
            if (now - lastSnapshotAt >= SNAPSHOT_INTERVAL_MILLIS) {
                session.updateProgress(
                        closedSet.size(),
                        openSet.size(),
                        current.nodeId(),
                        new ArrayList<>(recentVisited),
                        snapshotFrontier(openSet)
                );
                lastSnapshotAt = now;
            }
        }

        session.updateProgress(
                closedSet.size(),
                0,
                null,
                new ArrayList<>(recentVisited),
                List.of()
        );
        return AStarSearchResult.notFound();
    }

    private List<Long> reconstructPath(long goalNodeId, Map<Long, Long> cameFrom) {
        List<Long> path = new ArrayList<>();
        Long cursor = goalNodeId;
        while (cursor != null) {
            path.add(cursor);
            cursor = cameFrom.get(cursor);
        }
        java.util.Collections.reverse(path);
        return path;
    }

    private List<Long> snapshotFrontier(PriorityQueue<SearchNode> openSet) {
        return openSet.stream()
                .sorted(Comparator.comparingDouble(SearchNode::score))
                .map(SearchNode::nodeId)
                .limit(SNAPSHOT_LIMIT)
                .toList();
    }

    private ProjectionCandidate projectToNearestEdge(Point3D point) {
        ProjectionCandidate best = null;
        for (GraphEdge edge : campusGraphStore.getEdges()) {
            ProjectionCandidate candidate = projectToEdge(point, edge);
            if (candidate == null) {
                continue;
            }
            if (best == null || candidate.distanceToEdge() < best.distanceToEdge()) {
                best = candidate;
            }
        }

        if (best == null) {
            throw new IllegalStateException("No edge available for projection.");
        }
        return best;
    }

    private ProjectionCandidate projectToEdge(Point3D point, GraphEdge edge) {
        List<Point3D> geometry = edge.geometry();
        if (geometry == null || geometry.size() < 2) {
            return null;
        }

        double totalLength = 0.0;
        double[] prefixLengths = new double[geometry.size()];
        prefixLengths[0] = 0.0;
        for (int i = 0; i < geometry.size() - 1; i++) {
            totalLength += distance2D(geometry.get(i), geometry.get(i + 1));
            prefixLengths[i + 1] = totalLength;
        }

        double bestDistance = Double.POSITIVE_INFINITY;
        Point3D bestPoint = null;
        double bestLengthToProjection = 0.0;

        for (int i = 0; i < geometry.size() - 1; i++) {
            Point3D a = geometry.get(i);
            Point3D b = geometry.get(i + 1);
            SegmentProjection segmentProjection = projectToSegment(point, a, b);
            if (segmentProjection.distanceToPoint() < bestDistance) {
                bestDistance = segmentProjection.distanceToPoint();
                bestPoint = segmentProjection.projectedPoint();
                bestLengthToProjection = prefixLengths[i] + segmentProjection.lengthFromSegmentStart();
            }
        }

        if (bestPoint == null) {
            return null;
        }

        double accessToFromNode = bestLengthToProjection;
        double accessToToNode = Math.max(0.0, totalLength - bestLengthToProjection);

        return new ProjectionCandidate(
                edge.fromNodeId(),
                edge.toNodeId(),
                bestPoint,
                bestDistance,
                accessToFromNode,
                accessToToNode
        );
    }

    private SegmentProjection projectToSegment(Point3D point, Point3D start, Point3D end) {
        double ax = start.x();
        double ay = start.y();
        double bx = end.x();
        double by = end.y();
        double px = point.x();
        double py = point.y();

        double abx = bx - ax;
        double aby = by - ay;
        double apx = px - ax;
        double apy = py - ay;
        double abLengthSquared = abx * abx + aby * aby;
        double t = abLengthSquared <= EPS ? 0.0 : (apx * abx + apy * aby) / abLengthSquared;
        t = Math.max(0.0, Math.min(1.0, t));

        double projectedX = ax + t * abx;
        double projectedY = ay + t * aby;
        double projectedZ = start.z() + t * (end.z() - start.z());
        Point3D projectedPoint = new Point3D(projectedX, projectedY, projectedZ);

        return new SegmentProjection(
                projectedPoint,
                distance2D(point, projectedPoint),
                distance2D(start, projectedPoint)
        );
    }

    private double distance2D(Point3D left, Point3D right) {
        double dx = left.x() - right.x();
        double dy = left.y() - right.y();
        return Math.hypot(dx, dy);
    }

    private RouteSessionState findSession(UUID sessionId) {
        RouteSessionState session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Unknown route debug session: " + sessionId);
        }
        return session;
    }

    private record SearchNode(long nodeId, double score) {
    }

    private record SearchEdge(long toNodeId, double cost) {
    }

    private record SegmentProjection(Point3D projectedPoint, double distanceToPoint, double lengthFromSegmentStart) {
    }

    private record ProjectionOption(long endpointNodeId, double connectorCost) {
    }

    private record ProjectionCandidate(
            long edgeFromNodeId,
            long edgeToNodeId,
            Point3D projectedPoint,
            double distanceToEdge,
            double accessCostToFromNode,
            double accessCostToToNode
    ) {
        double inputToProjectionCost() {
            return distanceToEdge;
        }

        List<ProjectionOption> options() {
            return List.of(
                    new ProjectionOption(edgeFromNodeId, accessCostToFromNode),
                    new ProjectionOption(edgeToNodeId, accessCostToToNode)
            );
        }

        GraphMapProjectionResponse toResponse(GraphMapPointRequest inputPoint) {
            return new GraphMapProjectionResponse(
                    inputPoint,
                    new GraphMapPointResponse(projectedPoint.x(), projectedPoint.y(), projectedPoint.z()),
                    edgeFromNodeId,
                    edgeToNodeId,
                    distanceToEdge,
                    accessCostToFromNode,
                    accessCostToToNode
            );
        }
    }

    private record RouteCandidate(long startEndpointNodeId,
                                  long destinationEndpointNodeId,
                                  double totalCost,
                                  AStarSearchResult searchResult) {
    }

    private record RouteSearchGraph(Map<Long, List<SearchEdge>> adjacency) {
        static RouteSearchGraph from(List<GraphEdge> edges) {
            Map<Long, List<SearchEdge>> adjacency = new LinkedHashMap<>();

            for (GraphEdge edge : edges) {
                adjacency.computeIfAbsent(edge.fromNodeId(), key -> new ArrayList<>())
                        .add(new SearchEdge(edge.toNodeId(), edge.cost()));
                adjacency.computeIfAbsent(edge.toNodeId(), key -> new ArrayList<>())
                        .add(new SearchEdge(edge.fromNodeId(), edge.cost()));
            }

            return new RouteSearchGraph(adjacency);
        }

        List<SearchEdge> neighbors(long nodeId) {
            return adjacency.getOrDefault(nodeId, List.of());
        }
    }

    private record AStarSearchResult(boolean found,
                                     List<Long> pathNodeIds,
                                     List<GraphMapRouteStepResponse> steps,
                                     double totalCost) {
        static AStarSearchResult found(List<Long> pathNodeIds, Map<Long, Double> cameCost, double totalCost) {
            List<GraphMapRouteStepResponse> steps = new ArrayList<>();
            for (int i = 0; i < pathNodeIds.size() - 1; i++) {
                long fromNodeId = pathNodeIds.get(i);
                long toNodeId = pathNodeIds.get(i + 1);
                steps.add(new GraphMapRouteStepResponse(fromNodeId, toNodeId, cameCost.getOrDefault(toNodeId, 0.0)));
            }
            return new AStarSearchResult(true, pathNodeIds, steps, totalCost);
        }

        static AStarSearchResult notFound() {
            return new AStarSearchResult(false, List.of(), List.of(), Double.POSITIVE_INFINITY);
        }

        GraphMapRouteResultResponse toResponse() {
            return new GraphMapRouteResultResponse(found, totalCost, pathNodeIds, steps);
        }
    }

    private static final class RouteSessionState {
        private final UUID sessionId;
        private final Instant createdAt = Instant.now();
        private volatile GraphMapRouteSearchStatus status = GraphMapRouteSearchStatus.RUNNING;
        private volatile GraphMapProjectionResponse startProjection;
        private volatile GraphMapProjectionResponse destinationProjection;
        private volatile long visitedCount;
        private volatile int frontierSize;
        private volatile Long currentNodeId;
        private volatile List<Long> visitedNodeIds = List.of();
        private volatile List<Long> frontierNodeIds = List.of();
        private volatile String message = "Running A* search.";
        private volatile GraphMapRouteResultResponse result;

        private RouteSessionState(UUID sessionId) {
            this.sessionId = sessionId;
        }

        private synchronized void start(GraphMapProjectionResponse startProjection, GraphMapProjectionResponse destinationProjection) {
            this.startProjection = startProjection;
            this.destinationProjection = destinationProjection;
            this.status = GraphMapRouteSearchStatus.RUNNING;
            this.message = "Running A* search.";
        }

        private synchronized void updateProgress(long visitedCount,
                                                 int frontierSize,
                                                 Long currentNodeId,
                                                 List<Long> visitedNodeIds,
                                                 List<Long> frontierNodeIds) {
            this.visitedCount = visitedCount;
            this.frontierSize = frontierSize;
            this.currentNodeId = currentNodeId;
            this.visitedNodeIds = visitedNodeIds;
            this.frontierNodeIds = frontierNodeIds;
        }

        private synchronized void complete(GraphMapRouteResultResponse result) {
            this.status = GraphMapRouteSearchStatus.COMPLETED;
            this.result = result;
            this.message = "Route search completed.";
        }

        private synchronized void fail(String message) {
            this.status = GraphMapRouteSearchStatus.FAILED;
            this.message = message;
        }

        private synchronized GraphMapRouteSessionResponse snapshot() {
            return new GraphMapRouteSessionResponse(
                    sessionId,
                    status,
                    startProjection,
                    destinationProjection,
                    visitedCount,
                    frontierSize,
                    currentNodeId,
                    List.copyOf(visitedNodeIds),
                    List.copyOf(frontierNodeIds),
                    message,
                    result
            );
        }
    }
}
