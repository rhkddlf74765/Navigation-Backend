package com.example.campus_navigation_backend.test_package.graphmap.dto;

import com.example.campus_navigation_backend.test_package.graphmap.service.GraphMapRouteSearchStatus;

import java.util.UUID;

/**
 * 저장된 graph-map 경로 계산 세션을 표현한다.
 * <p>
 * 화면이 응용 계층 응답 클래스에 의존하지 않고 경로 세션을 식별하고 완료된 경로 결과를 렌더링할 수 있도록
 * 그래프 지도 에이피아이가 이 DTO를 반환한다.
 *
 * @param sessionId 이후 동일한 경로 결과를 조회하기 위한 식별자
 * @param status 세션의 현재 탐색 상태
 * @param message 세션 상태를 설명하는 표시용 또는 진단용 메시지
 * @param result 경로 탐색이 성공했을 때의 완료된 경로 결과
 */
public record GraphMapRouteSessionResponse(
        UUID sessionId,
        GraphMapRouteSearchStatus status,
        String message,
        GraphMapRouteResultResponse result
) {
}
