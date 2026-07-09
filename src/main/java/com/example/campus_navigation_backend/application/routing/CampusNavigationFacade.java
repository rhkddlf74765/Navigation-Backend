package com.example.campus_navigation_backend.application.routing;

import com.example.campus_navigation_backend.application.dto.RoutePoint;
import com.example.campus_navigation_backend.application.dto.RouteRequest;
import com.example.campus_navigation_backend.application.dto.RouteResponse;
import com.example.campus_navigation_backend.application.routing.helper.*;
import com.example.campus_navigation_backend.domain.graph.Point3D;
import com.example.campus_navigation_backend.log.service.RouteLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 라우팅 요청 해석부터 최종 응답 조립까지 경로 탐색 use case를 조율하는 application facade이다.
 * <p>
 * 컨트롤러가 사용하는 라우팅 진입점이며 endpoint 해석, 접근 경로 생성, 후보 경로 선택은 helper 구성요소에 위임한다.
 */
@Service
@RequiredArgsConstructor
public class CampusNavigationFacade {

    private final RouteRequestResolver routeRequestResolver;
    private final EndpointAccessPathResolver endpointAccessPathResolver;
    private final RouteCandidateEvaluator routeCandidateEvaluator;
    private final RouteLogService routeLogService;

    /**
     * 라우팅 요청을 해석하고 접근 경로와 그래프 내부 경로를 조합해 최적 경로 응답을 반환한다.
     */
    public RouteResponse findRoute(RouteRequest request) {
        UUID routeSessionId = UUID.randomUUID();
        Instant requestedAt = Instant.now();

        try {
            ResolvedRouteRequest resolvedRequest = routeRequestResolver.resolve(request);
            EndpointAccessPaths accessPaths = endpointAccessPathResolver.resolve(resolvedRequest);

            RouteCandidateResult bestRoute = routeCandidateEvaluator.findBestRoute(
                    accessPaths.startAccessPaths(),
                    accessPaths.destinationAccessPaths()
            );

            RouteResponse response = toResponse(routeSessionId, resolvedRequest, bestRoute);
            routeLogService.saveRouteReturned(request, response, requestedAt, Instant.now());
            return response;
        } catch (RuntimeException exception) {
            routeLogService.saveRouteFailed(request, routeSessionId, requestedAt, Instant.now(), exception);
            throw exception;
        }
    }

    /**
     * 컨트롤러가 application 내부 후보 객체에 의존하지 않도록 최종 후보 결과를 응답 DTO로 변환한다.
     */
    private RouteResponse toResponse(UUID routeSessionId, ResolvedRouteRequest request, RouteCandidateResult bestRoute) {
        return new RouteResponse(
                routeSessionId,
                request.destination().displayName(),
                bestRoute.destinationEndpointNodeId(),
                bestRoute.totalCost(),
                bestRoute.startAccessCost() + bestRoute.destinationAccessCost(),
                bestRoute.graphCost(),
                toRoutePoints(bestRoute.routePath().points())
        );
    }

    /**
     * 응답 매핑 책임이 라우팅 계산과 섞이지 않도록 metric 그래프 좌표를 경로 좌표 DTO로 변환한다.
     */
    private List<RoutePoint> toRoutePoints(List<Point3D> points) {
        return points.stream()
                .map(point -> new RoutePoint(point.lon(), point.lat(), point.ele()))
                .toList();
    }
}
