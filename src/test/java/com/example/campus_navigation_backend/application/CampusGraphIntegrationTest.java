//package com.example.campus_navigation_backend.application;
//
//import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
//import com.example.campus_navigation_backend.domain.graph.GraphNode;
//import com.example.campus_navigation_backend.domain.path.AStarPathFinder;
//import com.example.campus_navigation_backend.domain.path.PathResult;
//import com.example.campus_navigation_backend.support.PostgisTestContainerSupport;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.Comparator;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * Testcontainers의 PostGIS 테스트 데이터로 CampusGraph 로딩 결과를 검증하는 통합 테스트이다.
// */
//@SpringBootTest
//class CampusGraphIntegrationTest extends PostgisTestContainerSupport {
//
//    @Autowired
//    private CampusGraphStore campusGraphStore;
//
//    @Autowired
//    private AStarPathFinder pathFinder;
//
//    /**
//     * DB에서 생성된 노드와 엣지가 그래프 저장소에 로드되고 건물 입구 노드까지 A*로 도달 가능한지 검증한다.
//     */
//    @Test
//    void initializesGraphAndConnectsEntranceNode() {
//        List<GraphNode> nodes = campusGraphStore.getNodes();
//
//        assertThat(nodes).hasSize(4);
//        assertThat(campusGraphStore.getEdges()).hasSize(6);
//
//        List<Long> entranceNodeIds = campusGraphStore.findEntranceNodeIdsByBuildingName("Test Building");
//        assertThat(entranceNodeIds).hasSize(1);
//
//        long entranceNodeId = entranceNodeIds.get(0);
//        assertThat(campusGraphStore.getAdjacency(entranceNodeId)).isNotEmpty();
//
//        long startNodeId = nodes.stream()
//                .min(Comparator.comparingDouble(node -> node.point().lon()))
//                .orElseThrow()
//                .id();
//
//        PathResult pathResult = pathFinder.findPath(startNodeId, entranceNodeId);
//
//        assertThat(pathResult.found()).isTrue();
//        assertThat(pathResult.edges()).hasSize(3);
//    }
//}
