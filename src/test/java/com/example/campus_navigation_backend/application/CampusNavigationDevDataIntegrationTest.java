package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.support.DevDatabaseIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 개발 DB 데이터로 사용자 경로 탐색 흐름이 동작하는지 검증하는 통합 테스트이다.
 */
class CampusNavigationDevDataIntegrationTest extends DevDatabaseIntegrationTestSupport {

    @Autowired
    private CampusNavigationFacade campusNavigationFacade;

    @Value("${dev.test.start-longitude}")
    private double startLongitude;

    @Value("${dev.test.start-latitude}")
    private double startLatitude;

    @Value("${dev.test.start-altitude}")
    private double startAltitude;

    @Value("${dev.test.destination-building}")
    private String destinationBuildingName;

    /**
     * 개발 DB의 실제 좌표와 실제 목적지 건물명으로 최종 경로 응답이 생성되는지 검증한다.
     * <p>중점 검증 대상은 좌표 변환, projection 기반 출발 후보 생성, 목적지 입구 조회, A* 탐색, 응답 path 조립의 전체 흐름이다.
     */
    @Test
    void findsRouteUsingKnownDevDataScenario() {
        RouteRequest request = new RouteRequest(
                startLongitude,
                startLatitude,
                startAltitude,
                destinationBuildingName
        );

        RouteResponse response = campusNavigationFacade.findRoute(request);

        assertThat(response.destinationBuildingName()).isEqualTo(destinationBuildingName);
        assertThat(response.selectedEntranceId()).isGreaterThan(0);
        assertThat(response.totalDistanceMeters()).isGreaterThan(0.0);
        assertThat(response.path()).isNotEmpty();
    }
}
