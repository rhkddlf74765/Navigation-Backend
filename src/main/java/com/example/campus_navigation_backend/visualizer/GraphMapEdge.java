package com.example.campus_navigation_backend.visualizer;

import java.util.List;

/**
 * 지도에 선으로 표시할 그래프 엣지 정보이다.
 *
 * @param fromNodeId 시작 노드 ID
 * @param toNodeId 도착 노드 ID
 * @param edgeType 엣지 유형
 * @param cost 경로 탐색 비용
 * @param colorIndex 인접 엣지를 구분하기 위한 색상 인덱스
 * @param path WGS84 좌표 라인
 */
public record GraphMapEdge(
        long fromNodeId,
        long toNodeId,
        String edgeType,
        double cost,
        int colorIndex,
        List<MapPoint> path
) {
}
