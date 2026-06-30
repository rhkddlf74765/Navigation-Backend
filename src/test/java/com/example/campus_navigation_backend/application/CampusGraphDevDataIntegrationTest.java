package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.path.AStarPathFinder;
import com.example.campus_navigation_backend.domain.path.PathResult;
import com.example.campus_navigation_backend.support.DevDatabaseIntegrationTestSupport;
import com.example.campus_navigation_backend.visualizer.GraphMapFacade;
import com.example.campus_navigation_backend.visualizer.GraphMapResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 개발 DB 데이터로 그래프 생성 및 지도 시각화 응답의 유효성을 검증하는 통합 테스트이다.
 */
class CampusGraphDevDataIntegrationTest extends DevDatabaseIntegrationTestSupport {

    @Autowired
    private CampusGraphStore campusGraphStore;

    @Autowired
    private GraphMapFacade graphMapFacade;

    @Autowired
    private AStarPathFinder pathFinder;

    @Value("${dev.test.destination-building}")
    private String destinationBuildingName;

    /**
     * 개발 DB에서 생성된 그래프가 지도 응답으로 변환 가능한지 검증한다.
     */
    @Test
    void graphMapResponseContainsDevGraphNodesAndEdges() {
        GraphMapResponse response = graphMapFacade.getGraphForMap();

        assertThat(response.nodes()).isNotEmpty();
        assertThat(response.edges()).isNotEmpty();
        assertThat(response.edges())
                .allSatisfy(edge -> assertThat(edge.path()).hasSizeGreaterThanOrEqualTo(2));
    }

    /**
     * 개발 DB의 목적지 건물 입구 노드가 실제 그래프에 연결되어 있고 A*로 도달 가능한지 검증한다.
     */
    @Test
    void destinationEntrancesFromDevDataAreReachableFromGraphEdgeEndpoint() {
        List<Long> targetEntranceNodeIds = campusGraphStore.findEntranceNodeIdsByBuildingName(destinationBuildingName);

        assertThat(targetEntranceNodeIds).isNotEmpty();
        assertThat(targetEntranceNodeIds)
                .allSatisfy(nodeId -> assertThat(campusGraphStore.getAdjacency(nodeId)).isNotEmpty());

        long startNodeId = campusGraphStore.getEdges().stream()
                .mapToLong(GraphEdge::fromNodeId)
                .findFirst()
                .orElseThrow();

        boolean anyReachable = targetEntranceNodeIds.stream()
                .map(targetNodeId -> pathFinder.findPath(startNodeId, targetNodeId))
                .anyMatch(PathResult::found);

        assertThat(anyReachable).isTrue();
    }
}
