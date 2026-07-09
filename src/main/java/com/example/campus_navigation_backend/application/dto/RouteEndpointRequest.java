package com.example.campus_navigation_backend.application.dto;

/**
 * 라우팅 시작점과 도착점을 좌표 또는 건물명 중 하나의 형식으로 표현한다.
 * <p>
 * 출발지와 도착지가 같은 구조를 사용하도록 하여 resolver가 endpoint 종류에 따라 해석 방식을 선택할 수 있게 한다.
 *
 * @param type endpoint를 좌표로 해석할지 건물명으로 해석할지 구분하는 값
 * @param longitude 좌표 endpoint의 WGS84 경도
 * @param latitude 좌표 endpoint의 WGS84 위도
 * @param altitude 좌표 endpoint의 고도이며, resolver에서 null이면 0으로 처리한다
 * @param buildingName 건물 endpoint의 건물명
 */
public record RouteEndpointRequest(
        RouteEndpointType type,
        Double lon,
        Double lat,
        Double ele,
        String buildingName
) {
    /**
     * 호출자가 사용하지 않는 건물 필드를 직접 채우지 않아도 되도록 좌표 endpoint를 생성한다.
     */
    public static RouteEndpointRequest coordinate(Double lon, Double lat, Double ele) {
        return new RouteEndpointRequest(RouteEndpointType.COORDINATE, lon, lat, ele, null);
    }

    /**
     * 호출자가 사용하지 않는 좌표 필드를 직접 채우지 않아도 되도록 건물 endpoint를 생성한다.
     */
    public static RouteEndpointRequest building(String buildingName) {
        return new RouteEndpointRequest(RouteEndpointType.BUILDING, null, null, null, buildingName);
    }
}
