package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.GraphNode;
import com.example.campus_navigation_backend.domain.path.AStarPathFinder;
import com.example.campus_navigation_backend.domain.path.PathResult;
import com.example.campus_navigation_backend.support.PostgisTestContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testcontainers의 PostGIS 데이터를 이용해 CampusGraph 초기화 결과를 검증하는 통합 테스트이다.
 */
@SpringBootTest
class CampusGraphIntegrationTest extends PostgisTestContainerSupport {

    @Autowired
    private CampusGraphStore campusGraphStore;

    @Autowired
    private AStarPathFinder pathFinder;

    /**
     * DB의 라인과 입구 데이터가 메모리 그래프로 초기화되고 입구 노드까지 실제로 연결되는지 검증한다.
     * <p>중점 검증 대상은 생성된 노드/엣지 수, 건물명-입구 노드 매핑, 입구 노드의 adjacency, A* 도달 가능성이다.
     */
    @Test
    void initializesGraphAndConnectsEntranceNode() {
        List<GraphNode> nodes = campusGraphStore.getNodes();

        assertThat(nodes).hasSize(4);
        assertThat(campusGraphStore.getEdges()).hasSize(6);

        List<Long> entranceNodeIds = campusGraphStore.findEntranceNodeIdsByBuildingName("Test Building");
        assertThat(entranceNodeIds).hasSize(1);

        long entranceNodeId = entranceNodeIds.get(0);
        assertThat(campusGraphStore.getAdjacency(entranceNodeId)).isNotEmpty();

        long startNodeId = nodes.stream()
                .min(Comparator.comparingDouble(node -> node.point().x()))
                .orElseThrow()
                .id();

        PathResult pathResult = pathFinder.findPath(startNodeId, entranceNodeId);

        assertThat(pathResult.found()).isTrue();
        assertThat(pathResult.edges()).hasSize(3);
    }
}
