package com.example.campus_navigation_backend.domain.graph;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 초기화된 CampusGraph를 보관하고 그래프에 대한 읽기 명령만 제공하는 singleton bean이다.
 * <p>메모리에 유일하게 존재해야 하므로 singleton으로 설정하며, 초기화 이후에는 write를 허용하지 않는다.
 */
@Component
public class CampusGraphStore {

    private final AtomicReference<CampusGraph> graphReference = new AtomicReference<>();

    /**
     * 완성된 CampusGraph를 한 번만 등록한다.
     *
     * @param graph 초기화가 끝난 그래프
     */
    public void initialize(CampusGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("CampusGraph must not be null.");
        }

        if (!graphReference.compareAndSet(null, graph)) {
            throw new IllegalStateException("CampusGraph has already been initialized.");
        }
    }

    /**
     * 건물명에 대응하는 출입구 노드 ID 목록을 조회한다.
     *
     * @param buildingName 목적지 건물명
     * @return 출입구 노드 ID 목록
     */
    public List<Long> findEntranceNodeIdsByBuildingName(String buildingName) {
        return graph().findEntranceNodeIdsByBuildingName(buildingName);
    }

    /**
     * 노드 ID에 해당하는 그래프 노드를 조회한다.
     *
     * @param nodeId 노드 ID
     * @return 그래프 노드
     */
    public GraphNode getNode(long nodeId) {
        GraphNode node = graph().getNode(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("Graph node not found. nodeId=" + nodeId);
        }
        return node;
    }

    /**
     * 특정 노드에서 이동 가능한 인접 엣지를 조회한다.
     *
     * @param nodeId 시작 노드 ID
     * @return 인접 엣지 목록
     */
    public List<GraphEdge> getAdjacency(long nodeId) {
        return graph().getAdjacency(nodeId);
    }

    /**
     * 경로 탐색 시작점 후보로 사용할 수 있는 노드 ID 집합을 조회한다.
     *
     * @return 시작 후보 노드 ID 집합
     */
    public Set<Long> getStartCandidateNodeIds() {
        return graph().getStartCandidateNodeIds();
    }

    /**
     * 그래프에 등록된 모든 노드를 조회한다.
     *
     * @return 전체 그래프 노드 목록
     */
    public List<GraphNode> getNodes() {
        return graph().getNodes();
    }

    /**
     * 그래프에 등록된 모든 엣지를 조회한다.
     *
     * @return 전체 그래프 엣지 목록
     */
    public List<GraphEdge> getEdges() {
        return graph().getEdges();
    }

    /**
     * 두 그래프 노드 사이의 2D 직선 거리를 계산한다.
     *
     * @param fromNodeId 시작 노드 ID
     * @param toNodeId 도착 노드 ID
     * @return 2D 직선 거리
     */
    public double distance2D(long fromNodeId, long toNodeId) {
        return getNode(fromNodeId).point().distance2D(getNode(toNodeId).point());
    }

    /**
     * 그래프 초기화 여부를 검증하고 저장된 그래프를 반환한다.
     *
     * @return 초기화된 CampusGraph
     */
    private CampusGraph graph() {
        CampusGraph graph = graphReference.get();
        if (graph == null) {
            throw new IllegalStateException("CampusGraph has not been initialized yet.");
        }
        return graph;
    }
}
