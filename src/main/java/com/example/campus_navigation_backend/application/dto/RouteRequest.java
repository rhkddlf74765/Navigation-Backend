package com.example.campus_navigation_backend.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 라우팅에 필요한 출발 endpoint와 도착 endpoint를 함께 전달하는 요청 객체이다.
 * <p>
 * 양쪽 모두 {@link RouteEndpointRequest}를 사용하므로 출발지와 도착지를 좌표 또는 건물명으로 표현할 수 있다.
 *
 * @param start 사용자가 지정한 출발 endpoint
 * @param destination 사용자가 지정한 도착 endpoint
 */
public record RouteRequest(
        @Valid @NotNull RouteEndpointRequest start,
        @Valid @NotNull RouteEndpointRequest destination
) {
    /**
     * 기존 좌표-건물명 방식의 호출부가 깨지지 않도록 출발 좌표와 도착 건물명으로 요청을 생성한다.
     */
    public RouteRequest(Double longitude, Double latitude, Double altitude, String destinationBuildingName) {
        this(
                RouteEndpointRequest.coordinate(longitude, latitude, altitude),
                RouteEndpointRequest.building(destinationBuildingName)
        );
    }

    /**
     * 기존 혼합 요청 생성자를 유지하되, 도착 좌표가 하나라도 전달되면 좌표 도착지를 우선 사용한다.
     */
    public RouteRequest(Double longitude,
                        Double latitude,
                        Double altitude,
                        String destinationBuildingName,
                        Double destinationLongitude,
                        Double destinationLatitude,
                        Double destinationAltitude) {
        this(
                RouteEndpointRequest.coordinate(longitude, latitude, altitude),
                destinationLongitude != null || destinationLatitude != null
                        ? RouteEndpointRequest.coordinate(destinationLongitude, destinationLatitude, destinationAltitude)
                        : RouteEndpointRequest.building(destinationBuildingName)
        );
    }

    /**
     * 예전 평면적인 요청 구조를 읽는 기존 코드가 사용할 수 있도록 출발 경도를 노출한다.
     */
    public Double longitude() {
        return start.longitude();
    }

    /**
     * 예전 평면적인 요청 구조를 읽는 기존 코드가 사용할 수 있도록 출발 위도를 노출한다.
     */
    public Double latitude() {
        return start.latitude();
    }

    /**
     * 예전 평면적인 요청 구조를 읽는 기존 코드가 사용할 수 있도록 출발 고도를 노출한다.
     */
    public Double altitude() {
        return start.altitude();
    }

    /**
     * 예전 평면적인 요청 구조를 읽는 기존 코드가 사용할 수 있도록 도착 건물명을 노출한다.
     */
    public String destinationBuildingName() {
        return destination.buildingName();
    }
}
