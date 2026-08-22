//package com.example.campus_navigation_backend.domain.path;
//
//import com.example.campus_navigation_backend.domain.graph.CampusGraph;
//import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
//import com.example.campus_navigation_backend.domain.graph.GraphEdge;
//import com.example.campus_navigation_backend.domain.graph.Point3D;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
///**
// * DB나 Spring context 없이
// * A* 경로 탐색 알고리즘 자체를 검증한다.
// */
//class AStarPathFinderTest {
//
//    /**
//     * 실제 거리가 더 짧은 경로가 있더라도
//     * cost 합이 더 작은 경로를 선택하는지 검증한다.
//     */
//    @Test
//    void choosesRouteByCostNotByDistance() {
//
//        CampusGraphStore graphStore =
//                new CampusGraphStore();
//
//        CampusGraph.Builder builder =
//                CampusGraph.builder();
//
//        long nodeA =
//                builder.getOrCreateBaseNode(
//                        new Point3D(
//                                0.0,
//                                0.0,
//                                0.0
//                        )
//                );
//
//        long nodeB =
//                builder.getOrCreateBaseNode(
//                        new Point3D(
//                                1.0,
//                                0.0,
//                                0.0
//                        )
//                );
//
//        long nodeC =
//                builder.getOrCreateBaseNode(
//                        new Point3D(
//                                2.0,
//                                0.0,
//                                0.0
//                        )
//                );
//
//        long nodeD =
//                builder.getOrCreateBaseNode(
//                        new Point3D(
//                                1.0,
//                                1.0,
//                                0.0
//                        )
//                );
//
//        /*
//         * 실제 거리는 2m지만
//         * 가중 cost는 20.
//         */
//        addEdge(
//                builder,
//                1L,
//                nodeA,
//                nodeB,
//                1.0,
//                10.0
//        );
//
//        addEdge(
//                builder,
//                2L,
//                nodeB,
//                nodeC,
//                1.0,
//                10.0
//        );
//
//        /*
//         * 실제 거리는 6m로 더 길지만
//         * cost는 총 6.
//         */
//        addEdge(
//                builder,
//                3L,
//                nodeA,
//                nodeD,
//                3.0,
//                3.0
//        );
//
//        addEdge(
//                builder,
//                4L,
//                nodeD,
//                nodeC,
//                3.0,
//                3.0
//        );
//
//        graphStore.initialize(
//                builder.build()
//        );
//
//        PathFinder pathFinder =
//                new AStarPathFinder(
//                        graphStore
//                );
//
//        PathResult result =
//                pathFinder.findPath(
//                        nodeA,
//                        nodeC
//                );
//
//        assertThat(result.found())
//                .isTrue();
//
//        assertThat(result.totalCost())
//                .isEqualTo(6.0);
//
//        assertThat(
//                result.totalDistanceMeters()
//        ).isEqualTo(6.0);
//
//        assertThat(result.edges())
//                .extracting(
//                        GraphEdge::toNodeId
//                )
//                .containsExactly(
//                        nodeD,
//                        nodeC
//                );
//    }
//
//    /**
//     * 동일 노드가 더 낮은 gScore로 다시 발견됐을 때
//     * PriorityQueue의 오래된 항목 때문에 결과가 손상되지 않는지 검증한다.
//     */
//    @Test
//    void handlesStalePriorityQueueEntries() {
//
//        CampusGraphStore graphStore =
//                new CampusGraphStore();
//
//        CampusGraph.Builder builder =
//                CampusGraph.builder();
//
//        long start =
//                builder.getOrCreateBaseNode(
//                        new Point3D(
//                                0.0,
//                                0.0,
//                                0.0
//                        )
//                );
//
//        long nodeB =
//                builder.getOrCreateBaseNode(
//                        new Point3D(
//                                1.0,
//                                0.0,
//                                0.0
//                        )
//                );
//
//        long nodeA =
//                builder.getOrCreateBaseNode(
//                        new Point3D(
//                                2.0,
//                                0.0,
//                                0.0
//                        )
//                );
//
//        long goal =
//                builder.getOrCreateBaseNode(
//                        new Point3D(
//                                3.0,
//                                0.0,
//                                0.0
//                        )
//                );
//
//        /*
//         * A를 먼저 비싼 cost로 발견한다.
//         */
//        addEdge(
//                builder,
//                10L,
//                start,
//                nodeA,
//                2.0,
//                8.0
//        );
//
//        /*
//         * B를 거치면 A까지 cost가 2로 낮아진다.
//         */
//        addEdge(
//                builder,
//                11L,
//                start,
//                nodeB,
//                1.0,
//                1.0
//        );
//
//        addEdge(
//                builder,
//                12L,
//                nodeB,
//                nodeA,
//                1.0,
//                1.0
//        );
//
//        /*
//         * 이전의 비싼 A state가 큐에 남도록
//         * 이후 edge의 cost를 크게 설정한다.
//         */
//        addEdge(
//                builder,
//                13L,
//                nodeA,
//                goal,
//                1.0,
//                100.0
//        );
//
//        graphStore.initialize(
//                builder.build()
//        );
//
//        PathFinder pathFinder =
//                new AStarPathFinder(
//                        graphStore
//                );
//
//        PathResult result =
//                pathFinder.findPath(
//                        start,
//                        goal
//                );
//
//        assertThat(result.found())
//                .isTrue();
//
//        assertThat(result.totalCost())
//                .isEqualTo(102.0);
//
//        assertThat(
//                result.totalDistanceMeters()
//        ).isEqualTo(3.0);
//
//        assertThat(result.edges())
//                .extracting(
//                        GraphEdge::edgeId
//                )
//                .containsExactly(
//                        11L,
//                        12L,
//                        13L
//                );
//    }
//
//    /**
//     * 출발과 목적지가 같은 노드인 경우.
//     */
//    @Test
//    void returnsZeroLengthPathWhenStartEqualsGoal() {
//
//        CampusGraphStore graphStore =
//                new CampusGraphStore();
//
//        CampusGraph.Builder builder =
//                CampusGraph.builder();
//
//        long node =
//                builder.getOrCreateBaseNode(
//                        new Point3D(
//                                0.0,
//                                0.0,
//                                0.0
//                        )
//                );
//
//        graphStore.initialize(
//                builder.build()
//        );
//
//        PathFinder pathFinder =
//                new AStarPathFinder(
//                        graphStore
//                );
//
//        PathResult result =
//                pathFinder.findPath(
//                        node,
//                        node
//                );
//
//        assertThat(result.found())
//                .isTrue();
//
//        assertThat(result.totalCost())
//                .isZero();
//
//        assertThat(
//                result.totalDistanceMeters()
//        ).isZero();
//
//        assertThat(result.edges())
//                .isEmpty();
//    }
//
//    /**
//     * 목적지가 다른 connected component에 있는 경우.
//     */
//    @Test
//    void returnsUnreachableWhenGoalIsInDifferentComponent() {
//
//        CampusGraphStore graphStore =
//                new CampusGraphStore();
//
//        CampusGraph.Builder builder =
//                CampusGraph.builder();
//
//        long nodeA =
//                builder.getOrCreateBaseNode(
//                        new Point3D(
//                                0.0,
//                                0.0,
//                                0.0
//                        )
//                );
//
//        long nodeB =
//                builder.getOrCreateBaseNode(
//                        new Point3D(
//                                1.0,
//                                0.0,
//                                0.0
//                        )
//                );
//
//        long isolated =
//                builder.getOrCreateBaseNode(
//                        new Point3D(
//                                10.0,
//                                0.0,
//                                0.0
//                        )
//                );
//
//        addEdge(
//                builder,
//                20L,
//                nodeA,
//                nodeB,
//                1.0,
//                1.0
//        );
//
//        graphStore.initialize(
//                builder.build()
//        );
//
//        PathFinder pathFinder =
//                new AStarPathFinder(
//                        graphStore
//                );
//
//        PathResult result =
//                pathFinder.findPath(
//                        nodeA,
//                        isolated
//                );
//
//        assertThat(result.found())
//                .isFalse();
//
//        assertThat(result.edges())
//                .isEmpty();
//    }
//
//    /**
//     * cost < dist 상태가 생성되지 않는지 검증한다.
//     */
//    @Test
//    void rejectsEdgeWhoseCostIsLowerThanDistance() {
//
//        Point3D from =
//                new Point3D(
//                        0.0,
//                        0.0,
//                        0.0
//                );
//
//        Point3D to =
//                new Point3D(
//                        10.0,
//                        0.0,
//                        0.0
//                );
//
//        assertThatThrownBy(
//                () ->
//                        new GraphEdge(
//                                30L,
//                                1L,
//                                2L,
//                                10.0,
//                                9.0,
//                                "footway",
//                                List.of(
//                                        from,
//                                        to
//                                )
//                        )
//        )
//                .isInstanceOf(
//                        IllegalArgumentException.class
//                )
//                .hasMessageContaining(
//                        "cost must be greater than or equal to distanceMeters"
//                );
//    }
//
//    private static void addEdge(
//            CampusGraph.Builder builder,
//            long edgeId,
//            long fromNodeId,
//            long toNodeId,
//            double distanceMeters,
//            double cost
//    ) {
//        Point3D from =
//                builder.getNode(
//                        fromNodeId
//                ).point();
//
//        Point3D to =
//                builder.getNode(
//                        toNodeId
//                ).point();
//
//        builder.addDirectedEdge(
//                edgeId,
//                fromNodeId,
//                toNodeId,
//                distanceMeters,
//                cost,
//                "footway",
//                List.of(
//                        from,
//                        to
//                )
//        );
//    }
//}