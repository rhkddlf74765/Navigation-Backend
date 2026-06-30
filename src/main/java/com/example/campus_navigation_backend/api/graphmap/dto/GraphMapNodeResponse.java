package com.example.campus_navigation_backend.api.graphmap.dto;

/**
 * 그래프 지도 뷰어에서 사용하는 그래프 노드 스냅샷이다.
 */
public record GraphMapNodeResponse(long id, double x, double y, double z) {
}
