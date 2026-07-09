package com.example.campus_navigation_backend.log.domain;

/**
 * 라우팅 세션 중 기록할 수 있는 이벤트 종류를 나타낸다.
 */
public enum RouteEventType {
    /** 경로 요청이 들어온 이벤트 */
    ROUTE_REQUESTED,

    /** 경로 응답이 정상 반환된 이벤트 */
    ROUTE_RETURNED,

    /** 경로 계산이 실패한 이벤트 */
    ROUTE_FAILED,

    /** 사용자가 안내를 시작한 이벤트 */
    NAVIGATION_STARTED,

    /** 위치 샘플이 수신된 이벤트 */
    LOCATION_SAMPLE_RECEIVED,

    /** 사용자가 안내 경로에서 벗어난 이벤트 */
    OFF_ROUTE_DETECTED,

    /** 도착 후보 위치가 감지된 이벤트 */
    ARRIVAL_CANDIDATE_DETECTED,

    /** 도착이 확정된 이벤트 */
    ARRIVED,

    /** 재탐색이 요청된 이벤트 */
    REROUTE_REQUESTED,

    /** 안내가 사용자에 의해 취소된 이벤트 */
    NAVIGATION_CANCELLED,

    /** 안내 세션이 만료된 이벤트 */
    NAVIGATION_EXPIRED,

    /** 안내 세션이 종료된 이벤트 */
    NAVIGATION_ENDED
}
