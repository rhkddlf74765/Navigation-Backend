package com.example.campus_navigation_backend.application;

import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.application.routing.CampusNavigationFacade;
import com.example.campus_navigation_backend.support.PostgisTestContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 사용자의 경로 요청이 Facade를 통해 projection, 목적지 조회, A* 탐색, 응답 조립까지 수행되는지 검증하는 통합 테스트이다.
 */
@SpringBootTest
class CampusNavigationFacadeIntegrationTest extends PostgisTestContainerSupport {

    @Autowired
    private CampusNavigationFacade campusNavigationFacade;

    /**
     * 임의의 출발 좌표에서 목적지 건물명까지 경로 응답을 생성할 수 있는지 검증한다.
     */
    @Test
    void findsRouteFromArbitraryPointToDestinationBuilding() {
        RouteRequest request = new RouteRequest(0.0002, 0.0001, 0.0, "Test Building");

        RouteResponse response = campusNavigationFacade.findRoute(request);

        assertThat(response.destinationBuildingName()).isEqualTo("Test Building");
        assertThat(response.selectedEntranceId()).isGreaterThan(0);
        assertThat(response.totalDistanceMeters()).isGreaterThan(0.0);
        assertThat(response.path()).isNotEmpty();
    }
}
