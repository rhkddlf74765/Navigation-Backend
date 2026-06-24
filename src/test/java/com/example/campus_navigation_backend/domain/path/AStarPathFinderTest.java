package com.example.campus_navigation_backend.domain.path;

import com.example.campus_navigation_backend.domain.graph.CampusGraph;
import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DB나 Spring context 없이 A* 경로 탐색 알고리즘 자체를 검증하는 단위 테스트이다.
 */
class AStarPathFinderTest {

    /**
     * 연결된 그래프에서 A*가 목적지까지 도달 가능한 경로를 찾는지 검증한다.
     * <p>중점 검증 대상은 더 비싼 우회 경로가 존재하더라도 최소 비용 경로의 edge 순서와 비용을 선택하는지 여부이다.
     */
    @Test
    void findsShortestPathBetweenConnectedNodes() {
        CampusGraphStore graphStore = new CampusGraphStore();
        CampusGraph.Builder builder = CampusGraph.builder();

        long nodeA = builder.getOrCreateBaseNode(new Point3D(0.0, 0.0, 0.0));
        long nodeB = builder.getOrCreateBaseNode(new Point3D(1.0, 0.0, 0.0));
        long nodeC = builder.getOrCreateBaseNode(new Point3D(2.0, 0.0, 0.0));
        long nodeD = builder.getOrCreateBaseNode(new Point3D(1.0, 1.0, 0.0));

        builder.addDirectedEdge(nodeA, nodeB, 1.0, "footway", List.of(
                new Point3D(0.0, 0.0, 0.0),
                new Point3D(1.0, 0.0, 0.0)
        ));
        builder.addDirectedEdge(nodeB, nodeC, 1.0, "footway", List.of(
                new Point3D(1.0, 0.0, 0.0),
                new Point3D(2.0, 0.0, 0.0)
        ));
        builder.addDirectedEdge(nodeA, nodeD, 5.0, "footway", List.of(
                new Point3D(0.0, 0.0, 0.0),
                new Point3D(1.0, 1.0, 0.0)
        ));
        builder.addDirectedEdge(nodeD, nodeC, 5.0, "footway", List.of(
                new Point3D(1.0, 1.0, 0.0),
                new Point3D(2.0, 0.0, 0.0)
        ));

        graphStore.initialize(builder.build());
        AStarPathFinder pathFinder = new AStarPathFinder(graphStore);

        PathResult result = pathFinder.findPath(nodeA, nodeC);

        assertThat(result.found()).isTrue();
        assertThat(result.totalCost()).isEqualTo(2.0);
        assertThat(result.edges()).hasSize(2);
        assertThat(result.edges())
                .extracting(edge -> edge.toNodeId())
                .containsExactly(nodeB, nodeC);
    }

    /**
     * 출발 노드와 목적지 노드가 서로 다른 컴포넌트에 있을 때 unreachable 결과를 반환하는지 검증한다.
     * <p>중점 검증 대상은 A*가 임의의 경로를 만들지 않고 실패 상태와 빈 edge 목록을 명확히 반환하는지 여부이다.
     */
    @Test
    void returnsUnreachableWhenGoalIsInDifferentComponent() {
        CampusGraphStore graphStore = new CampusGraphStore();
        CampusGraph.Builder builder = CampusGraph.builder();

        long nodeA = builder.getOrCreateBaseNode(new Point3D(0.0, 0.0, 0.0));
        long nodeB = builder.getOrCreateBaseNode(new Point3D(1.0, 0.0, 0.0));
        long isolated = builder.getOrCreateBaseNode(new Point3D(10.0, 0.0, 0.0));

        builder.addDirectedEdge(nodeA, nodeB, 1.0, "footway", List.of(
                new Point3D(0.0, 0.0, 0.0),
                new Point3D(1.0, 0.0, 0.0)
        ));

        graphStore.initialize(builder.build());
        AStarPathFinder pathFinder = new AStarPathFinder(graphStore);

        PathResult result = pathFinder.findPath(nodeA, isolated);

        assertThat(result.found()).isFalse();
        assertThat(result.edges()).isEmpty();
    }
}
