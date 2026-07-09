package com.example.campus_navigation_backend.log.domain;

/**
 * 라우팅 세션이 종료된 이유를 나타낸다.
 */
public enum RouteEndReason {
    /** 목적지 도착으로 정상 종료 */
    ARRIVED,

    /** 사용자의 명시적 취소로 종료 */
    USER_CANCELLED,

    /** 재탐색 요청으로 기존 세션 종료 */
    REROUTED,

    /** 시간 초과로 종료 */
    TIMEOUT,

    /** 오류 발생으로 종료 */
    ERROR
}
