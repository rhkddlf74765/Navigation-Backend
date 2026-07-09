package com.example.campus_navigation_backend.log.domain;

/**
 * 라우팅 요청에서 endpoint가 출발지인지 목적지인지 구분한다.
 */
public enum RouteEndpointRole {
    /** 경로의 출발 endpoint */
    START,

    /** 경로의 목적지 endpoint */
    DESTINATION
}
