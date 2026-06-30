package com.example.campus_navigation_backend.visualizer;

import com.example.campus_navigation_backend.domain.graph.CampusGraphStore;
import com.example.campus_navigation_backend.domain.graph.GraphEdge;
import com.example.campus_navigation_backend.domain.graph.GraphNode;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.domain.projection.PointProjection;
import com.example.campus_navigation_backend.domain.projection.PointProjector;
import com.example.campus_navigation_backend.repository.NavigationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 메모리 그래프 전체를 지도 시각화용 WGS84 좌표 응답으로 변환한다.
 */
@Service
@RequiredArgsConstructor
public class GraphMapFacade {

    private final CampusGraphStore campusGraphStore;
    private final NavigationRepository navigationRepository;
    private final PointProjector pointProjector;

    /**
     * 캠퍼스 그래프의 모든 노드와 엣지를 지도 렌더링용 응답으로 만든다.
     *
     * @return 그래프 지도 응답
     */
    public GraphMapResponse getGraphForMap() {
        List<GraphNode> graphNodes = campusGraphStore.getNodes();
        List<MapPoint> nodePoints = transform(graphNodes.stream()
                .map(GraphNode::point)
                .toList());

        List<GraphMapNode> nodes = new ArrayList<>();
        for (int i = 0; i < graphNodes.size(); i++) {
            GraphNode node = graphNodes.get(i);
            nodes.add(new GraphMapNode(node.id(), node.type(), nodePoints.get(i)));
        }

        List<GraphEdge> graphEdges = campusGraphStore.getEdges().stream()
                .filter(edge -> edge.geometry() != null && edge.geometry().size() >= 2)
                .toList();

        List<Point3D> flattenedEdgePoints = graphEdges.stream()
                .flatMap(edge -> edge.geometry().stream())
                .toList();
        List<MapPoint> transformedEdgePoints = transform(flattenedEdgePoints);
        List<Integer> colorIndexes = assignColorIndexes(graphEdges);

        List<GraphMapEdge> edges = new ArrayList<>();
        int offset = 0;
        for (int i = 0; i < graphEdges.size(); i++) {
            GraphEdge edge = graphEdges.get(i);
            int size = edge.geometry().size();
            List<MapPoint> path = transformedEdgePoints.subList(offset, offset + size);
            edges.add(new GraphMapEdge(
                    edge.fromNodeId(),
                    edge.toNodeId(),
                    edge.edgeType(),
                    edge.cost(),
                    colorIndexes.get(i),
                    List.copyOf(path)
            ));
            offset += size;
        }

        return new GraphMapResponse(nodes, edges);
    }

    /**
     * 지도에서 클릭한 좌표를 가장 가까운 그래프 엣지 위로 투영한다.
     *
     * @param request 클릭 좌표 요청
     * @return 지도에 표시할 projection 결과
     */
    public ProjectionMapResponse projectPointForMap(ProjectionMapRequest request) {
        Point3D metricPoint = navigationRepository
                .transformToMetric(request.longitude(), request.latitude(), request.resolvedAltitude())
                .toPoint3D();

        PointProjection projection = campusGraphStore.getEdges().stream()
                .filter(edge -> edge.geometry() != null && edge.geometry().size() >= 2)
                .map(edge -> pointProjector.project(metricPoint, edge))
                .min((left, right) -> Double.compare(left.distanceFromSource(), right.distanceFromSource()))
                .orElseThrow(() -> new IllegalStateException("No graph edge available for projection."));

        List<MapPoint> sourceAndProjected = transform(List.of(metricPoint, projection.projectedPoint()));
        List<MapPoint> edgePath = transform(projection.sourceEdge().geometry());

        return new ProjectionMapResponse(
                sourceAndProjected.get(0),
                sourceAndProjected.get(1),
                projection.sourceEdge().fromNodeId(),
                projection.sourceEdge().toNodeId(),
                projection.sourceEdge().edgeType(),
                projection.sourceEdge().cost(),
                projection.distanceFromSource(),
                projection.costFromEdgeStart(),
                projection.costToEdgeEnd(),
                edgePath
        );
    }

    /**
     * 노드를 공유하는 인접 엣지끼리 가능한 한 다른 색상 인덱스를 갖도록 greedy coloring을 수행한다.
     *
     * @param edges 색상을 배정할 엣지 목록
     * @return 엣지 순서와 동일한 색상 인덱스 목록
     */
    private List<Integer> assignColorIndexes(List<GraphEdge> edges) {
        Map<Long, Set<Integer>> usedColorIndexesByNodeId = new HashMap<>();
        List<Integer> colorIndexes = new ArrayList<>();

        for (GraphEdge edge : edges) {
            Set<Integer> unavailable = new HashSet<>();
            unavailable.addAll(usedColorIndexesByNodeId.getOrDefault(edge.fromNodeId(), Set.of()));
            unavailable.addAll(usedColorIndexesByNodeId.getOrDefault(edge.toNodeId(), Set.of()));

            int colorIndex = firstAvailableColorIndex(unavailable);
            colorIndexes.add(colorIndex);

            usedColorIndexesByNodeId
                    .computeIfAbsent(edge.fromNodeId(), ignored -> new HashSet<>())
                    .add(colorIndex);
            usedColorIndexesByNodeId
                    .computeIfAbsent(edge.toNodeId(), ignored -> new HashSet<>())
                    .add(colorIndex);
        }

        return colorIndexes;
    }

    /**
     * 이미 사용 중인 색상 인덱스를 피해 가장 작은 색상 인덱스를 선택한다.
     *
     * @param unavailable 사용 불가능한 색상 인덱스 집합
     * @return 사용할 색상 인덱스
     */
    private int firstAvailableColorIndex(Set<Integer> unavailable) {
        int colorIndex = 0;
        while (unavailable.contains(colorIndex)) {
            colorIndex++;
        }
        return colorIndex;
    }

    /**
     * 미터 단위 좌표 목록을 WGS84 지도 지점 목록으로 변환한다.
     *
     * @param points metric 좌표 목록
     * @return WGS84 좌표 목록
     */
    private List<MapPoint> transform(List<Point3D> points) {
        return navigationRepository.transformMetricPointsToWgs84(points).stream()
                .map(Wgs84PointRow::toMapPoint)
                .toList();
    }
}
