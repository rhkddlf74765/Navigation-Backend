package com.example.campus_navigation_backend.log.domain;

/**
 * 경로 결과 로그의 path JSON 배열에 저장되는 좌표 값이다.
 * <p>
 * {@link PointLog}는 JPA embedded 컬럼 매핑에 사용되므로, Hibernate JSON 직렬화와 충돌하지 않도록
 * path 전용 record를 별도로 둔다.
 */
public record RoutePathPointLog(
        Double lon,
        Double lat,
        Double ele
) {
    /**
     * 경로 좌표 로그 값을 생성한다.
     */
    public static RoutePathPointLog of(Double lon, Double lat, Double ele) {
        return new RoutePathPointLog(lon, lat, ele);
    }
}
