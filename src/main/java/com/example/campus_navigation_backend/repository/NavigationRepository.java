package com.example.campus_navigation_backend.repository;

import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.repository.dto.BuildingPointRow;
import com.example.campus_navigation_backend.repository.dto.GraphEdgeRow;
import com.example.campus_navigation_backend.repository.dto.GraphNodeRow;
import com.example.campus_navigation_backend.repository.dto.TransformedPointRow;
import com.example.campus_navigation_backend.visualizer.Wgs84PointRow;

import java.util.List;

/**
 * 캠퍼스 그래프를 구성하고 라우팅 좌표를 변환하는 데 필요한 영속성 작업을 정의한다.
 */
public interface NavigationRepository {

    /**
     * 메모리 캠퍼스 그래프 초기화에 사용할 모든 그래프 노드를 조회한다.
     */
    List<GraphNodeRow> findAllGraphNodes();

    /**
     * 인접 관계와 경로 탐색 비용 초기화에 사용할 모든 그래프 엣지를 조회한다.
     */
    List<GraphEdgeRow> findAllGraphEdges();

    /**
     * 라우팅 요청이 건물 endpoint를 메모리에서 해석할 수 있도록 이름이 있는 건물 지점을 조회한다.
     */
    List<BuildingPointRow> findAllBuildingPoints();

    /**
     * 요청 좌표를 그래프 투영과 A*에서 사용하는 metric 좌표계로 변환한다.
     */
    TransformedPointRow transformToMetric(double lon, double lat, double ele);

    /**
     * 지도 API가 경로를 렌더링할 수 있도록 metric 경로 좌표를 WGS84 좌표로 다시 변환한다.
     */
    List<Wgs84PointRow> transformMetricPointsToWgs84(List<Point3D> metricPoints);
}
