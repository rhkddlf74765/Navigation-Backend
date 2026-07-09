package com.example.campus_navigation_backend.log.domain;

/**
 * 라우팅 세션의 현재 생명주기 상태를 나타낸다.
 */
public enum RouteSessionStatus {
    /** 경로 요청이 접수되어 처리 중인 상태 */
    REQUESTED,

    /** 경로 계산이 성공적으로 완료되어 응답이 반환된 상태 */
    ROUTE_RETURNED,

    /** 경로 계산에 실패한 상태 */
    ROUTE_FAILED,

    /** 사용자가 반환된 경로를 따라 이동 중인 상태 */
    NAVIGATING,

    /** 사용자가 목적지에 도착해 라우팅이 종료된 상태 */
    ARRIVED,

    /** 사용자가 직접 라우팅을 취소한 상태 */
    CANCELLED,

    /** 시간 초과 등으로 라우팅 세션이 만료된 상태 */
    EXPIRED
}
