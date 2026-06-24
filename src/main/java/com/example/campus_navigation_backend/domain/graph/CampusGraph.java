package com.example.campus_navigation_backend.domain.graph;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 캠퍼스 보행 네트워크를 표현하는 읽기 전용 그래프이다.
 * 노드, 엣지, 도로 선분 endpoint, 건물명과 출입구 노드의 매핑을 가진다.
 */
public class CampusGraph {

    /**
     * 그래프의 모든 노드를 저장한다.
     */
    private final Map<Long, GraphNode> nodes;
    /**
     * 각 노드에서 이동 가능한 엣지 목록
     * key: 출발 node Id
     * value: 출발 노드에서 나가는 GraphEdge 목록
     */
    private final Map<Long, List<GraphEdge>> adjacency;
    /**
     * DB에서 읽은 라인 ID와, 그 라인이 그래프에서 어떤 시작/끝 노드로 변환됐는지 저장한다.
     */
    private final Map<Long, LineEndpoints> lineEndpointsByLineId;
    /**
     * 건물명과 입구 노드 ID 목록을 매핑한다.
     */
    private final Map<String, List<Long>> buildingToEntranceNodeIds;
    /**
     * -- GETTER --
     *  경로 탐색 시작 후보로 사용할 수 있는 노드 ID 목록을 조회한다.
     *
     * @return 시작 후보 노드 ID 집합
     */
    @Getter
    private final Set<Long> startCandidateNodeIds;

    /**
     * Builder가 수집한 변경 가능한 자료구조를 불변 복사하여 그래프를 생성한다.
     *
     * @param builder 그래프 생성용 빌더
     */
    private CampusGraph(Builder builder) {
        this.nodes = Map.copyOf(builder.nodes);
        this.adjacency = copyAdjacency(builder.adjacency);
        this.lineEndpointsByLineId = Map.copyOf(builder.lineEndpointsByLineId);
        this.buildingToEntranceNodeIds = copyBuildingEntrances(builder.buildingToEntranceNodeIds);
        this.startCandidateNodeIds = Set.copyOf(builder.startCandidateNodeIds);
    }

    /**
     * 그래프 생성용 Builder를 만든다.
     *
     * @return 새 그래프 빌더
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 노드 ID로 그래프 노드를 조회한다.
     *
     * @param nodeId 노드 ID
     * @return 그래프 노드 또는 null
     */
    public GraphNode getNode(long nodeId) {
        return nodes.get(nodeId);
    }

    /**
     * 특정 노드에서 나가는 엣지 목록을 조회한다.
     *
     * @param nodeId 시작 노드 ID
     * @return 인접 엣지 목록
     */
    public List<GraphEdge> getAdjacency(long nodeId) {
        return adjacency.getOrDefault(nodeId, List.of());
    }

    /**
     * 원본 도로 선분 ID에 대응하는 시작/끝 노드를 조회한다.
     *
     * @param lineId 원본 도로 선분 ID
     * @return 도로 선분 endpoint 정보
     */
    public LineEndpoints getLineEndpoints(long lineId) {
        return lineEndpointsByLineId.get(lineId);
    }

    /**
     * 건물명에 대응하는 출입구 노드 ID 목록을 조회한다.
     *
     * @param buildingName 사용자가 입력한 건물명
     * @return 출입구 노드 ID 목록
     */
    public List<Long> findEntranceNodeIdsByBuildingName(String buildingName) {
        String normalized = normalize(buildingName);
        return buildingToEntranceNodeIds.getOrDefault(normalized, List.of());
    }

    /**
     * 그래프에 등록된 모든 노드를 조회한다.
     *
     * @return 전체 그래프 노드 목록
     */
    public List<GraphNode> getNodes() {
        return List.copyOf(nodes.values());
    }

    /**
     * 그래프에 등록된 모든 엣지를 조회한다.
     *
     * @return 전체 그래프 엣지 목록
     */
    public List<GraphEdge> getEdges() {
        return adjacency.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    /**
     * 인접 리스트를 외부에서 수정할 수 없도록 복사한다.
     *
     * @param source 원본 인접 리스트
     * @return 불변 인접 리스트
     */
    private static Map<Long, List<GraphEdge>> copyAdjacency(Map<Long, List<GraphEdge>> source) {
        Map<Long, List<GraphEdge>> copied = new HashMap<>();
        source.forEach((nodeId, edges) -> copied.put(nodeId, List.copyOf(edges)));
        return Map.copyOf(copied);
    }

    /**
     * 건물명과 출입구 노드 매핑을 외부에서 수정할 수 없도록 복사한다.
     *
     * @param source 원본 건물명 매핑
     * @return 불변 건물명 매핑
     */
    private static Map<String, List<Long>> copyBuildingEntrances(Map<String, List<Long>> source) {
        Map<String, List<Long>> copied = new HashMap<>();
        source.forEach((buildingName, entranceNodeIds) -> copied.put(buildingName, List.copyOf(entranceNodeIds)));
        return Map.copyOf(copied);
    }

    /**
     * 건물명 매칭을 위해 공백과 대소문자를 정규화한다.
     *
     * @param value 원본 문자열
     * @return 정규화된 문자열
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * CampusGraph 생성 중에만 노드와 엣지를 추가하는 빌더이다.
     */
    public static class Builder {
        private final AtomicLong nodeSequence = new AtomicLong(1);
        private final Map<NodeKey, Long> baseNodeIndex = new HashMap<>();
        private final Map<Long, GraphNode> nodes = new HashMap<>();
        private final Map<Long, List<GraphEdge>> adjacency = new HashMap<>();
        private final Map<Long, LineEndpoints> lineEndpointsByLineId = new HashMap<>();
        private final Map<String, List<Long>> buildingToEntranceNodeIds = new HashMap<>();
        private final Set<Long> startCandidateNodeIds = new HashSet<>();

        /**
         * 좌표가 같은 base 노드가 있으면 재사용하고, 없으면 새로 생성한다.
         *
         * @param point base 노드 좌표
         * @return base 노드 ID
         */
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

        /**
         * 건물 출입구 노드를 생성한다.
         *
         * @param point 출입구 좌표
         * @param entranceId 원본 출입구 ID
         * @param buildingName 출입구가 속한 건물명
         * @return 생성된 출입구 노드 ID
         */
        public Long findMatchingBaseNodeId(Point3D point, double toleranceMeters) {
            long bestNodeId = -1L;
            double bestDistance = Double.POSITIVE_INFINITY;

            for (GraphNode node : nodes.values()) {
                if (node.type() != GraphNodeType.BASE) {
                    continue;
                }

                double distance = point.distance2D(node.point());
                if (distance <= toleranceMeters && distance < bestDistance) {
                    bestDistance = distance;
                    bestNodeId = node.id();
                }
            }

            return bestNodeId == -1L ? null : bestNodeId;
        }

        public void addNode(long nodeId, GraphNodeType type, long sourceId, Point3D point) {
            GraphNode node = new GraphNode(nodeId, type, sourceId, point);
            nodes.put(nodeId, node);
            adjacency.putIfAbsent(nodeId, new ArrayList<>());
            if (type != GraphNodeType.PROJECTION) {
                startCandidateNodeIds.add(nodeId);
                baseNodeIndex.put(NodeKey.from(point), nodeId);
            }
            nodeSequence.updateAndGet(current -> Math.max(current, nodeId + 1));
        }

        public GraphNode getNode(long nodeId) {
            return nodes.get(nodeId);
        }

        public long createEntranceNode(Point3D point, long entranceId, String buildingName) {
            long nodeId = nodeSequence.getAndIncrement();
            GraphNode node = new GraphNode(nodeId, GraphNodeType.ENTRANCE, entranceId, point);
            nodes.put(nodeId, node);
            adjacency.put(nodeId, new ArrayList<>());
            startCandidateNodeIds.add(nodeId);
            return nodeId;
        }

        /**
         * 출입구나 현재 위치를 도로 선분 위에 투영한 projection 노드를 생성한다.
         *
         * @param point projection 좌표
         * @return 생성된 projection 노드 ID
         */
        public long createProjectionNode(Point3D point) {
            long nodeId = nodeSequence.getAndIncrement();
            GraphNode node = new GraphNode(nodeId, GraphNodeType.PROJECTION, nodeId, point);
            nodes.put(nodeId, node);
            adjacency.put(nodeId, new ArrayList<>());
            return nodeId;
        }

        /**
         * 방향성을 가진 그래프 엣지를 추가한다.
         *
         * @param fromNodeId 시작 노드 ID
         * @param toNodeId 도착 노드 ID
         * @param cost 경로 탐색 비용
         * @param edgeType 엣지 유형 또는 highway 값
         * @param geometry 엣지를 구성하는 좌표 목록
         */
        public void addDirectedEdge(long fromNodeId, long toNodeId, double cost, String edgeType, List<Point3D> geometry) {
            adjacency.computeIfAbsent(fromNodeId, key -> new ArrayList<>())
                    .add(new GraphEdge(fromNodeId, toNodeId, cost, edgeType, List.copyOf(geometry)));
        }

        /**
         * 원본 도로 선분 ID와 그래프 endpoint 노드 ID를 연결한다.
         *
         * @param lineId 원본 도로 선분 ID
         * @param endpoints 그래프 endpoint 정보
         */
        public void putLineEndpoints(long lineId, LineEndpoints endpoints) {
            lineEndpointsByLineId.put(lineId, endpoints);
        }

        /**
         * 원본 도로 선분 ID에 대응하는 endpoint 정보를 조회한다.
         *
         * @param lineId 원본 도로 선분 ID
         * @return 그래프 endpoint 정보
         */
        public LineEndpoints getLineEndpoints(long lineId) {
            return lineEndpointsByLineId.get(lineId);
        }

        /**
         * 정규화된 건물명과 출입구 노드 ID를 매핑한다.
         *
         * @param normalizedBuildingName 정규화된 건물명
         * @param entranceNodeId 출입구 노드 ID
         */
        public void addBuildingEntrance(String normalizedBuildingName, long entranceNodeId) {
            buildingToEntranceNodeIds
                    .computeIfAbsent(normalizedBuildingName, key -> new ArrayList<>())
                    .add(entranceNodeId);
        }

        /**
         * 수집한 노드와 엣지를 불변 CampusGraph로 만든다.
         *
         * @return 완성된 CampusGraph
         */
        public CampusGraph build() {
            return new CampusGraph(this);
        }
    }
}
